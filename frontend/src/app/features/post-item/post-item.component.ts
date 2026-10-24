import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  computed,
  effect,
  inject,
  signal,
  untracked
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { PostService } from '../../core/services/post.service';
import { AutoFillSuggestion, MediaService } from '../../core/services/media.service';
import { AuthService } from '../../core/services/auth.service';
import { CameraCaptureDirective } from '../../core/directives/camera-capture.directive';
import {
  CATEGORIES,
  Category,
  Condition,
  CreatePostRequest,
  CreatePostResponse
} from '../../core/models/post.model';

/** The posting flow: sign in, then photo, then review what Gemini filled in. */
export type PostStep = 'signin' | 'photo' | 'review';

export const CAMPUS_SPOTS = ['Union', 'Commons', 'BARH', 'Quad', 'Blitman', 'Folsom Library', 'ECAV', 'DCC'];

@Component({
  selector: 'app-post-item',
  standalone: true,
  imports: [RouterLink, ReactiveFormsModule, CameraCaptureDirective],
  templateUrl: './post-item.component.html',
  styleUrl: './post-item.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PostItemComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly postService = inject(PostService);
  private readonly mediaService = inject(MediaService);
  readonly authService = inject(AuthService);

  readonly categories = CATEGORIES;
  readonly conditions: { value: Condition; label: string }[] = [
    { value: 'LIKE_NEW', label: 'Like new' },
    { value: 'GOOD', label: 'Good' },
    { value: 'FAIR', label: 'Fair' }
  ];
  readonly campusSpots = CAMPUS_SPOTS;

  readonly photoUrl = signal<string | null>(null);
  readonly photoPreview = signal<string | null>(null);
  readonly isUploadingPhoto = signal(false);
  readonly uploadError = signal<string | null>(null);

  readonly autofillEnabled = signal(true);
  readonly isAutofilling = signal(false);
  readonly aiFilled = signal(false);
  readonly aiMessage = signal<string | null>(null);

  readonly isSubmitting = signal(false);
  readonly submitError = signal<string | null>(null);
  readonly createdPost = signal<CreatePostResponse | null>(null);

  readonly step = computed<PostStep>(() => {
    if (!this.authService.isAuthenticated()) return 'signin';
    return this.photoUrl() && !this.isAutofilling() ? 'review' : 'photo';
  });
  readonly stepNumber = computed(() => ({ signin: 1, photo: 2, review: 3 })[this.step()]);

  readonly postForm = this.fb.group({
    title: ['', [Validators.required, Validators.maxLength(100)]],
    description: ['', [Validators.required, Validators.maxLength(1000)]],
    category: [this.categories[0] as Category, [Validators.required]],
    condition: ['GOOD' as Condition, [Validators.required]],
    isFree: [true],
    price: [{ value: 0, disabled: true }, [Validators.required, Validators.min(0)]],
    pickupLocation: ['', [Validators.required, Validators.maxLength(150)]],
    availableUntil: ['', [Validators.required]],
    legalConfirmed: [false, [Validators.requiredTrue]]
  });

  constructor() {
    // Re-check the AI flag once the user is signed in (the endpoint is public, but this keeps state fresh).
    effect(() => {
      if (this.authService.isAuthenticated()) {
        untracked(() => this.checkAutofillStatus());
      }
    });
  }

  ngOnInit(): void {
    const deadline = new Date();
    deadline.setDate(deadline.getDate() + 14);
    this.postForm.patchValue({ availableUntil: deadline.toISOString().split('T')[0] });

    this.postForm.get('isFree')?.valueChanges.subscribe((isFree) => this.setFree(!!isFree));
  }

  get minDate(): string {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    return tomorrow.toISOString().split('T')[0];
  }

  openSignIn(): void {
    this.authService.openAuthModal();
  }

  checkAutofillStatus(): void {
    this.mediaService.getAutofillStatus().subscribe({
      next: (status) => this.autofillEnabled.set(status.enabled),
      error: () => this.autofillEnabled.set(false)
    });
  }

  toggleAutofill(): void {
    this.mediaService.toggleAutofill(!this.autofillEnabled()).subscribe({
      next: (res) => this.autofillEnabled.set(res.enabled)
    });
  }

  onPhotoCaptured(file: File): void {
    this.uploadError.set(null);
    this.aiFilled.set(false);
    this.aiMessage.set(null);
    this.photoPreview.set(URL.createObjectURL(file));
    this.isUploadingPhoto.set(true);

    this.mediaService.uploadPhoto(file).subscribe({
      next: (res) => {
        const url = res.photoUrl || res.url || null;
        this.isUploadingPhoto.set(false);
        if (url && this.autofillEnabled()) {
          this.isAutofilling.set(true);
          this.photoUrl.set(url);
          this.runAutofill(url);
        } else {
          this.photoUrl.set(url);
        }
      },
      error: (err) => {
        this.isUploadingPhoto.set(false);
        this.photoPreview.set(null);
        this.uploadError.set(this.uploadErrorMessage(err.status));
      }
    });
  }

  onCaptureError(message: string): void {
    this.uploadError.set(message);
  }

  /** Asks Gemini to read the uploaded photo and fills the form with its suggestion. */
  runAutofill(photoUrl: string): void {
    this.isAutofilling.set(true);
    this.mediaService.autoFill(photoUrl).subscribe({
      next: (suggestion) => {
        this.applySuggestion(suggestion);
        this.isAutofilling.set(false);
      },
      error: () => {
        this.isAutofilling.set(false);
        this.aiMessage.set('Gemini could not read this photo — please fill in the details.');
      }
    });
  }

  applySuggestion(s: AutoFillSuggestion): void {
    const isFree = !s.suggestedPrice || s.suggestedPrice <= 0;
    this.postForm.patchValue({
      title: s.title,
      description: s.description,
      category: (this.categories.includes(s.category as Category) ? s.category : 'Other') as Category,
      condition: (['LIKE_NEW', 'GOOD', 'FAIR'].includes(s.condition) ? s.condition : 'GOOD') as Condition,
      isFree
    });
    if (!isFree) {
      this.postForm.patchValue({ price: Math.round(s.suggestedPrice) });
    }
    this.aiFilled.set(true);
    this.aiMessage.set('Gemini filled in the details from your photo. Check them, then post.');
  }

  retakePhoto(): void {
    this.photoUrl.set(null);
    this.photoPreview.set(null);
    this.aiFilled.set(false);
    this.aiMessage.set(null);
  }

  setCondition(condition: Condition): void {
    this.postForm.patchValue({ condition });
  }

  setSpot(spot: string): void {
    this.postForm.patchValue({ pickupLocation: spot });
  }

  submitPost(): void {
    const user = this.authService.currentUser();
    if (!user || !this.photoUrl()) return;
    if (this.postForm.invalid) {
      this.postForm.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    this.submitError.set(null);
    const val = this.postForm.getRawValue();

    const request: CreatePostRequest = {
      title: val.title!.trim(),
      description: val.description!.trim(),
      category: val.category as Category,
      condition: val.condition as Condition,
      price: val.isFree ? 0 : Number(val.price || 0),
      pickupLocation: val.pickupLocation!.trim(),
      availableUntil: new Date(val.availableUntil + 'T23:59:59Z').toISOString(),
      photoUrl: this.photoUrl()!,
      posterName: user.name,
      posterContact: user.email,
      legalConfirmed: !!val.legalConfirmed
    };

    this.postService.createPost(request).subscribe({
      next: (res) => {
        this.isSubmitting.set(false);
        this.createdPost.set(res);
      },
      error: (err) => {
        this.isSubmitting.set(false);
        this.submitError.set(err.error?.message || 'Could not post your item. Please check the fields.');
      }
    });
  }

  private setFree(isFree: boolean): void {
    const price = this.postForm.get('price');
    if (isFree) {
      price?.setValue(0);
      price?.disable({ emitEvent: false });
    } else {
      price?.enable({ emitEvent: false });
    }
  }

  private uploadErrorMessage(status: number): string {
    if (status === 413) return 'That photo is over 5 MB. Try a smaller one.';
    if (status === 415) return 'Please use a JPEG, PNG or WebP photo.';
    if (status === 401) return 'Your session expired. Please sign in again.';
    return 'Upload failed. Please try again.';
  }
}
