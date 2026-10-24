import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { InterestService } from './interest.service';
import { CreateInterestRequest, InterestDetail, InterestSummary } from '../models/interest.model';

describe('InterestService', () => {
  let service: InterestService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        InterestService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(InterestService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should submit interest inquiry to POST /api/posts/{id}/interests', () => {
    const request: CreateInterestRequest = {
      name: 'Maya Lin',
      contact: 'linm@rpi.edu',
      message: 'Can I pick it up tonight?'
    };

    const mockSummary: InterestSummary = {
      count: 3,
      firstNames: ['Alex', 'Chris', 'Maya']
    };

    service.createInterest('post-123', request).subscribe((summary) => {
      expect(summary.count).toBe(3);
      expect(summary.firstNames).toContain('Maya');
    });

    const req = httpMock.expectOne('/api/posts/post-123/interests');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockSummary);
  });

  it('should fetch interest summary from GET /api/posts/{id}/interests/summary', () => {
    const mockSummary: InterestSummary = {
      count: 2,
      firstNames: ['Alex', 'Chris']
    };

    service.getSummary('post-123').subscribe((summary) => {
      expect(summary.count).toBe(2);
      expect(summary.firstNames).toEqual(['Alex', 'Chris']);
    });

    const req = httpMock.expectOne('/api/posts/post-123/interests/summary');
    expect(req.request.method).toBe('GET');
    req.flush(mockSummary);
  });

  it('should fetch full interest inquiries with X-Edit-Token header', () => {
    const mockInquiries: InterestDetail[] = [
      {
        id: 'int-1',
        name: 'Maya Lin',
        contact: 'linm@rpi.edu',
        message: 'Interested!',
        createdAt: '2026-10-23T01:00:00Z'
      }
    ];

    service.getInterests('post-123', 'valid-token').subscribe((list) => {
      expect(list.length).toBe(1);
      expect(list[0].contact).toBe('linm@rpi.edu');
    });

    const req = httpMock.expectOne('/api/posts/post-123/interests');
    expect(req.request.method).toBe('GET');
    expect(req.request.headers.get('X-Edit-Token')).toBe('valid-token');
    req.flush(mockInquiries);
  });
});
