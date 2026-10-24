import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { MyActivityComponent } from './my-activity.component';
import { AuthService } from '../../core/services/auth.service';
import { UserService } from '../../core/services/user.service';
import { of } from 'rxjs';
import { provideRouter } from '@angular/router';
import { UserInquiryItem, UserPostItem } from '../../core/models/user.model';

describe('MyActivityComponent', () => {
  let component: MyActivityComponent;
  let fixture: ComponentFixture<MyActivityComponent>;
  let authService: AuthService;
  let userService: jasmine.SpyObj<UserService>;

  const mockPosts: UserPostItem[] = [
    {
      id: 'post-1',
      title: 'Wooden Desk',
      category: 'Furniture',
      price: 0,
      isFree: true,
      status: 'AVAILABLE',
      photoUrl: 'https://example.com/desk.jpg',
      createdAt: '2026-10-23T12:00:00Z',
      interestCount: 1,
      inquiries: [
        {
          id: 'inq-1',
          inquirerName: 'Sam Freshman',
          inquirerEmail: 'sam@rpi.edu',
          message: 'Can pick up tomorrow at 5pm!',
          createdAt: '2026-10-23T13:00:00Z'
        }
      ]
    }
  ];

  const mockInterests: UserInquiryItem[] = [
    {
      interestId: 'inq-2',
      postId: 'post-2',
      postTitle: 'Desk Lamp',
      postCategory: 'Dorm essentials',
      postPrice: 10,
      postPhotoUrl: 'https://example.com/lamp.jpg',
      postStatus: 'AVAILABLE',
      message: 'Still available for pick up?',
      createdAt: '2026-10-23T14:00:00Z'
    }
  ];

  beforeEach(async () => {
    localStorage.clear();
    const userSpy = jasmine.createSpyObj('UserService', [
      'getMyPosts',
      'getMyInterests',
      'updatePostStatus',
      'deletePost'
    ]);
    userSpy.getMyPosts.and.returnValue(of(mockPosts));
    userSpy.getMyInterests.and.returnValue(of(mockInterests));
    userSpy.updatePostStatus.and.returnValue(of({ id: 'post-1', status: 'TAKEN' }));
    userSpy.deletePost.and.returnValue(of(void 0));

    await TestBed.configureTestingModule({
      imports: [MyActivityComponent],
      providers: [
        AuthService,
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: UserService, useValue: userSpy },
        provideRouter([])
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(MyActivityComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService);
    userService = TestBed.inject(UserService) as jasmine.SpyObj<UserService>;
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should render sign-in prompt when unauthenticated', () => {
    fixture.detectChanges();
    const el: HTMLElement = fixture.nativeElement;
    expect(el.querySelector('.sign-in-prompt-card')).toBeTruthy();
    expect(el.querySelector('.tabs-header')).toBeNull();
  });

  it('should load posts and inquiries when authenticated', () => {
    authService.setCredential('test-token:123:user@rpi.edu:User');
    component.ngOnInit();
    fixture.detectChanges();

    expect(userService.getMyPosts).toHaveBeenCalled();
    expect(userService.getMyInterests).toHaveBeenCalled();

    const el: HTMLElement = fixture.nativeElement;
    expect(el.querySelector('.sign-in-prompt-card')).toBeNull();
    expect(el.querySelector('.tabs-header')).toBeTruthy();

    const postTitles = el.querySelectorAll('.post-title');
    expect(postTitles.length).toBe(1);
    expect(postTitles[0].textContent).toContain('Wooden Desk');

    const inquiries = el.querySelectorAll('.inquiry-row');
    expect(inquiries.length).toBe(1);
    expect(inquiries[0].textContent).toContain('Sam Freshman');
    expect(inquiries[0].textContent).toContain('Can pick up tomorrow');
  });

  it('should switch to Requested Items tab and render requested items', () => {
    authService.setCredential('test-token:123:user@rpi.edu:User');
    component.ngOnInit();
    fixture.detectChanges();

    component.switchTab('requests');
    fixture.detectChanges();

    expect(component.activeTab()).toBe('requests');
    const el: HTMLElement = fixture.nativeElement;
    const reqTitles = el.querySelectorAll('.req-title');
    expect(reqTitles.length).toBe(1);
    expect(reqTitles[0].textContent).toContain('Desk Lamp');
    expect(el.querySelector('.note-text')?.textContent).toContain('Still available for pick up?');
  });

  it('should mark post as taken when action button is clicked', () => {
    authService.setCredential('test-token:123:user@rpi.edu:User');
    component.ngOnInit();
    fixture.detectChanges();

    const markBtn = fixture.nativeElement.querySelector('.mark-taken-btn') as HTMLButtonElement;
    expect(markBtn).toBeTruthy();

    markBtn.click();
    fixture.detectChanges();

    expect(userService.updatePostStatus).toHaveBeenCalledWith('post-1', 'TAKEN');
    expect(component.myPosts()[0].status).toBe('TAKEN');
  });

  it('should delete post after confirmation', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    authService.setCredential('test-token:123:user@rpi.edu:User');
    component.ngOnInit();
    fixture.detectChanges();

    const deleteBtn = fixture.nativeElement.querySelector('.delete-btn') as HTMLButtonElement;
    expect(deleteBtn).toBeTruthy();

    deleteBtn.click();
    fixture.detectChanges();

    expect(userService.deletePost).toHaveBeenCalledWith('post-1');
    expect(component.myPosts().length).toBe(0);
  });
});
