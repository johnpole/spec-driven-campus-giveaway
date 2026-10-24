import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { PostService } from './post.service';
import { CreatePostRequest, CreatePostResponse, PostDetail, PostSummary } from '../models/post.model';

describe('PostService', () => {
  let service: PostService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        PostService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(PostService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch feed with category and freeOnly query parameters', () => {
    const mockPosts: PostSummary[] = [
      {
        id: 'post-1',
        title: 'Desk',
        category: 'Furniture',
        condition: 'GOOD',
        price: 0,
        isFree: true,
        pickupLocation: 'Cary Hall',
        availableUntil: '2026-11-01T00:00:00Z',
        photoUrl: '/api/media/desk.jpg',
        interestCount: 2,
        createdAt: '2026-10-23T00:00:00Z',
        status: 'AVAILABLE'
      }
    ];

    service.getFeed('Furniture', true, 'desk').subscribe((posts) => {
      expect(posts.length).toBe(1);
      expect(posts[0].title).toBe('Desk');
    });

    const req = httpMock.expectOne(request =>
      request.url === '/api/posts' &&
      request.params.get('category') === 'Furniture' &&
      request.params.get('freeOnly') === 'true' &&
      request.params.get('q') === 'desk'
    );
    expect(req.request.method).toBe('GET');
    req.flush(mockPosts);
  });

  it('should retrieve post details by id', () => {
    const mockDetail: PostDetail = {
      id: 'post-123',
      title: 'Gaming Monitor',
      description: 'Works great',
      category: 'Electronics',
      condition: 'LIKE_NEW',
      price: 50,
      isFree: false,
      pickupLocation: 'BARH Lobby',
      availableUntil: '2026-11-01T00:00:00Z',
      photoUrl: '/api/media/monitor.jpg',
      status: 'AVAILABLE',
      posterName: 'Jordan Chen',
      interestCount: 1,
      createdAt: '2026-10-23T00:00:00Z'
    };

    service.getPost('post-123').subscribe((detail) => {
      expect(detail.id).toBe('post-123');
      expect(detail.posterName).toBe('Jordan Chen');
    });

    const req = httpMock.expectOne('/api/posts/post-123');
    expect(req.request.method).toBe('GET');
    req.flush(mockDetail);
  });

  it('should post new item and return create response with edit token', () => {
    const createReq: CreatePostRequest = {
      title: 'Fan',
      description: 'Quiet tower fan',
      category: 'Dorm essentials',
      condition: 'GOOD',
      price: 10,
      pickupLocation: 'Warren Hall',
      availableUntil: '2026-11-01T00:00:00Z',
      photoUrl: '/api/media/fan.jpg',
      posterName: 'Casey',
      posterContact: 'casey@rpi.edu',
      legalConfirmed: true
    };

    const mockResponse: CreatePostResponse = {
      id: 'fan-1',
      editToken: 'token-uuid-123'
    };

    service.createPost(createReq).subscribe((res) => {
      expect(res.id).toBe('fan-1');
      expect(res.editToken).toBe('token-uuid-123');
    });

    const req = httpMock.expectOne('/api/posts');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(createReq);
    req.flush(mockResponse);

    // loadFeed() is triggered via tap()
    const feedReq = httpMock.expectOne('/api/posts');
    expect(feedReq.request.method).toBe('GET');
    feedReq.flush([]);
  });

  it('should update post status with X-Edit-Token header', () => {
    const mockUpdated: PostDetail = {
      id: 'post-1',
      title: 'Desk',
      description: 'Study desk',
      category: 'Furniture',
      condition: 'GOOD',
      price: 0,
      isFree: true,
      pickupLocation: 'Cary Hall',
      availableUntil: '2026-11-01T00:00:00Z',
      photoUrl: '/api/media/desk.jpg',
      status: 'CLAIMED',
      posterName: 'Alex',
      interestCount: 1,
      createdAt: '2026-10-23T00:00:00Z'
    };

    service.updateStatus('post-1', 'CLAIMED', 'valid-token').subscribe((res) => {
      expect(res.status).toBe('CLAIMED');
    });

    const req = httpMock.expectOne('/api/posts/post-1/status');
    expect(req.request.method).toBe('PATCH');
    expect(req.request.headers.get('X-Edit-Token')).toBe('valid-token');
    expect(req.request.body).toEqual({ status: 'CLAIMED' });
    req.flush(mockUpdated);
  });

  it('should delete post with X-Edit-Token header', () => {
    service.deletePost('post-1', 'valid-token').subscribe();

    const req = httpMock.expectOne('/api/posts/post-1');
    expect(req.request.method).toBe('DELETE');
    expect(req.request.headers.get('X-Edit-Token')).toBe('valid-token');
    req.flush(null);

    // loadFeed() triggered
    const feedReq = httpMock.expectOne('/api/posts');
    feedReq.flush([]);
  });
});
