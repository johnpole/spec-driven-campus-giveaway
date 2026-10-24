import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { of, throwError } from 'rxjs';
import { PostItemComponent } from './post-item.component';
import { PostService } from '../../core/services/post.service';
import { MediaService } from '../../core/services/media.service';
import { AuthService } from '../../core/services/auth.service';

describe('PostItemComponent (sign in → photo → AI → review)', () => {
  let component: PostItemComponent;
  let fixture: ComponentFixture<PostItemComponent>;
  let mediaService: MediaService;
  let postService: PostService;
  let authService: AuthService;
  const photo = new File(['img'], 'bike.jpg', { type: 'image/jpeg' });

  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [PostItemComponent],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()]
    }).compileComponents();

    mediaService = TestBed.inject(MediaService);
    postService = TestBed.inject(PostService);
    authService = TestBed.inject(AuthService);
    spyOn(mediaService, 'getAutofillStatus').and.returnValue(of({ enabled: true, remainingSeconds: 0 }));

    fixture = TestBed.createComponent(PostItemComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  function signIn(): void {
    authService.setCredential('test-token:u-1:sam@rpi.edu:Sam Student');
    fixture.detectChanges();
  }

  it('starts at the sign-in step and opens the auth modal', () => {
    expect(component.step()).toBe('signin');
    const btn: HTMLButtonElement = fixture.nativeElement.querySelector('#post-signin-btn');
    btn.click();
    expect(authService.authModalOpen()).toBeTrue();
  });

  it('moves to the photo step once signed in', () => {
    signIn();
    expect(component.step()).toBe('photo');
    expect(fixture.nativeElement.querySelector('.photo-drop')).toBeTruthy();
  });

  it('uploads the photo, then asks Gemini with the uploaded URL and fills the form', () => {
    signIn();
    spyOn(mediaService, 'uploadPhoto').and.returnValue(of({ photoUrl: '/api/media/bike.jpg' }));
    spyOn(mediaService, 'autoFill').and.returnValue(of({
      title: 'Trek FX 2 bike', category: 'Bikes', condition: 'GOOD', suggestedPrice: 80, description: 'Commuter bike.'
    }));

    component.onPhotoCaptured(photo);

    expect(mediaService.autoFill).toHaveBeenCalledWith('/api/media/bike.jpg');
    expect(component.step()).toBe('review');
    expect(component.aiFilled()).toBeTrue();
    expect(component.postForm.get('title')?.value).toBe('Trek FX 2 bike');
    expect(component.postForm.get('category')?.value).toBe('Bikes');
    expect(component.postForm.get('isFree')?.value).toBeFalse();
    expect(component.postForm.get('price')?.value).toBe(80);
  });

  it('falls back to manual entry when Gemini fails', () => {
    signIn();
    spyOn(mediaService, 'uploadPhoto').and.returnValue(of({ photoUrl: '/api/media/bike.jpg' }));
    spyOn(mediaService, 'autoFill').and.returnValue(throwError(() => new Error('down')));

    component.onPhotoCaptured(photo);

    expect(component.step()).toBe('review');
    expect(component.aiFilled()).toBeFalse();
    expect(component.aiMessage()).toContain('fill in the details');
  });

  it('maps an unknown AI category to Other', () => {
    component.applySuggestion({ title: 'Thing', category: 'Appliances', condition: 'NEW', suggestedPrice: 0, description: 'x' });
    expect(component.postForm.get('category')?.value).toBe('Other');
    expect(component.postForm.get('condition')?.value).toBe('GOOD');
    expect(component.postForm.get('isFree')?.value).toBeTrue();
  });

  it('submits with the signed-in identity and shows the success screen', () => {
    signIn();
    spyOn(mediaService, 'uploadPhoto').and.returnValue(of({ photoUrl: '/api/media/bike.jpg' }));
    spyOn(mediaService, 'autoFill').and.returnValue(of({
      title: 'Desk', category: 'Furniture', condition: 'GOOD', suggestedPrice: 0, description: 'Sturdy desk.'
    }));
    spyOn(postService, 'createPost').and.returnValue(of({ id: 'p-1', editToken: 't' }));

    component.onPhotoCaptured(photo);
    component.postForm.patchValue({ pickupLocation: 'Union', legalConfirmed: true });
    component.submitPost();
    fixture.detectChanges();

    const req = (postService.createPost as jasmine.Spy).calls.mostRecent().args[0];
    expect(req.posterName).toBe('Sam Student');
    expect(req.posterContact).toBe('sam@rpi.edu');
    expect(req.photoUrl).toBe('/api/media/bike.jpg');
    expect(fixture.nativeElement.querySelector('.success-screen')).toBeTruthy();
  });
});
