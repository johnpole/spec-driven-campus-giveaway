import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { MediaService } from './media.service';
import { MediaUploadResponse } from '../models/media.model';

describe('MediaService', () => {
  let service: MediaService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        MediaService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(MediaService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should upload photo via multipart FormData', () => {
    const file = new File(['dummy photo data'], 'item.jpg', { type: 'image/jpeg' });
    const mockResponse: MediaUploadResponse = {
      url: '/api/media/upload-123.jpg',
      filename: 'upload-123.jpg',
      contentType: 'image/jpeg',
      size: 16
    };

    service.uploadPhoto(file).subscribe((res) => {
      expect(res.url).toBe('/api/media/upload-123.jpg');
      expect(res.filename).toBe('upload-123.jpg');
    });

    const req = httpMock.expectOne('/api/media/upload');
    expect(req.request.method).toBe('POST');
    expect(req.request.body instanceof FormData).toBeTrue();
    req.flush(mockResponse);
  });

  it('should fetch autofill status', () => {
    service.getAutofillStatus().subscribe((status) => {
      expect(status.enabled).toBeTrue();
      expect(status.remainingSeconds).toBe(600);
    });

    const req = httpMock.expectOne('/api/media/autofill/status');
    expect(req.request.method).toBe('GET');
    req.flush({ enabled: true, remainingSeconds: 600 });
  });

  it('should toggle autofill status', () => {
    service.toggleAutofill(true, 5).subscribe((res) => {
      expect(res.enabled).toBeTrue();
    });

    const req = httpMock.expectOne('/api/media/autofill/toggle?enabled=true&minutes=5');
    expect(req.request.method).toBe('POST');
    req.flush({ enabled: true, remainingSeconds: 300 });
  });

  it('should send the uploaded photo URL as JSON to the autofill endpoint', () => {
    service.autoFill('/api/media/desk.jpg').subscribe((res) => {
      expect(res.title).toBe('Study Desk');
      expect(res.category).toBe('Furniture');
    });

    const req = httpMock.expectOne('/api/media/autofill');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ photoUrl: '/api/media/desk.jpg' });
    req.flush({
      title: 'Study Desk',
      category: 'Furniture',
      description: 'Sturdy wooden desk',
      suggestedPrice: 0,
      condition: 'GOOD'
    });
  });
});
