import { Directive, EventEmitter, HostBinding, HostListener, Input, Output } from '@angular/core';
import { downscaleImage } from '../utils/image-resizer.util';

@Directive({
  selector: 'input[type=file][appCameraCapture]',
  standalone: true
})
export class CameraCaptureDirective {
  @HostBinding('attr.accept')
  readonly accept = 'image/*';

  @HostBinding('attr.capture')
  readonly capture = 'environment';

  @Input() maxDimension = 1600;
  @Input() quality = 0.85;

  @Output() photoCaptured = new EventEmitter<File>();
  @Output() captureError = new EventEmitter<string>();

  @HostListener('change', ['$event'])
  async onFileChange(event: Event): Promise<void> {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) {
      return;
    }

    const file = input.files[0];
    try {
      const resizedFile = await downscaleImage(file, this.maxDimension, this.quality);
      this.photoCaptured.emit(resizedFile);
    } catch (err: any) {
      this.captureError.emit(err?.message || 'Error processing photo');
    }
  }
}
