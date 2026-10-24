import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CameraCaptureDirective } from './camera-capture.directive';

@Component({
  standalone: true,
  imports: [CameraCaptureDirective],
  template: `
    <input
      type="file"
      appCameraCapture
      (photoCaptured)="onPhotoCaptured($event)"
      (captureError)="onError($event)">
  `
})
class TestHostComponent {
  capturedFile: File | null = null;
  errorMessage: string | null = null;

  onPhotoCaptured(file: File): void {
    this.capturedFile = file;
  }

  onError(err: string): void {
    this.errorMessage = err;
  }
}

describe('CameraCaptureDirective', () => {
  let fixture: ComponentFixture<TestHostComponent>;
  let inputElement: HTMLInputElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestHostComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(TestHostComponent);
    fixture.detectChanges();
    inputElement = fixture.nativeElement.querySelector('input');
  });

  it('should set accept to image/* and capture to environment', () => {
    expect(inputElement.getAttribute('accept')).toBe('image/*');
    expect(inputElement.getAttribute('capture')).toBe('environment');
  });

  it('should process image and emit photoCaptured event on change', async () => {
    const canvas = document.createElement('canvas');
    canvas.width = 100;
    canvas.height = 100;
    const ctx = canvas.getContext('2d')!;
    ctx.fillStyle = '#D6001C';
    ctx.fillRect(0, 0, 100, 100);
    const blob = await new Promise<Blob>((resolve) => canvas.toBlob((b) => resolve(b!), 'image/jpeg'));
    const testFile = new File([blob], 'photo.jpg', { type: 'image/jpeg' });

    const dataTransfer = new DataTransfer();
    dataTransfer.items.add(testFile);
    inputElement.files = dataTransfer.files;

    const capturedPromise = new Promise<File>((resolve) => {
      const originalHandler = fixture.componentInstance.onPhotoCaptured.bind(fixture.componentInstance);
      fixture.componentInstance.onPhotoCaptured = (f: File) => {
        originalHandler(f);
        resolve(f);
      };
    });

    inputElement.dispatchEvent(new Event('change'));

    const file = await capturedPromise;
    expect(file).toBeTruthy();
    expect(file.name).toBe('photo.jpg');
    expect(fixture.componentInstance.capturedFile).toBe(file);
  });
});
