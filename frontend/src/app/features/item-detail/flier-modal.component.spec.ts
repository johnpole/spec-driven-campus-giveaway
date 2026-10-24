import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FlierModalComponent } from './flier-modal.component';
import { ComponentRef } from '@angular/core';
import { PostDetail } from '../../core/models/post.model';

describe('FlierModalComponent', () => {
  let component: FlierModalComponent;
  let componentRef: ComponentRef<FlierModalComponent>;
  let fixture: ComponentFixture<FlierModalComponent>;

  const mockPost: PostDetail = {
    id: 'lamp-456',
    title: 'Study Desk Lamp',
    description: 'Adjustable LED desk lamp',
    category: 'Electronics',
    condition: 'LIKE_NEW',
    price: 0,
    isFree: true,
    pickupLocation: 'Sharp Hall 102',
    availableUntil: '2026-11-01T00:00:00Z',
    photoUrl: 'https://example.com/lamp.jpg',
    status: 'AVAILABLE',
    posterName: 'Jordan Smith',
    interestCount: 0,
    createdAt: '2026-10-23T10:00:00Z'
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FlierModalComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(FlierModalComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;

    componentRef.setInput('isOpen', true);
    componentRef.setInput('post', mockPost);
    fixture.detectChanges();
  });

  it('should render printable flier with title, location, and price banner', () => {
    const el: HTMLElement = fixture.nativeElement;
    expect(el.querySelector('.item-title')?.textContent).toContain('Study Desk Lamp');
    expect(el.querySelector('.price-banner')?.textContent).toContain('FREE GEAR');
    expect(el.querySelector('.meta-val')?.textContent).toContain('Sharp Hall 102');
  });

  it('should compute scannable QR code URL containing item ID', () => {
    expect(component.itemUrl()).toContain('/items/lamp-456');
    expect(component.qrCodeUrl()).toContain('api.qrserver.com');
    expect(component.qrCodeUrl()).toContain(encodeURIComponent(component.itemUrl()));
  });

  it('should render tear-off slips with item details', () => {
    const el: HTMLElement = fixture.nativeElement;
    const slips = el.querySelectorAll('.slip');
    expect(slips.length).toBe(4);
    expect(slips[0].textContent).toContain('Study Desk Lamp');
    expect(slips[0].textContent).toContain('FREE');
    expect(slips[0].textContent).toContain('Sharp Hall 102');
  });

  it('should call window.print when print button is clicked', () => {
    spyOn(window, 'print');
    const printBtn = fixture.nativeElement.querySelector('.print-btn') as HTMLButtonElement;
    expect(printBtn).toBeTruthy();

    printBtn.click();
    expect(window.print).toHaveBeenCalled();
  });

  it('should emit closed when close button is clicked', () => {
    spyOn(component.closed, 'emit');
    const closeBtn = fixture.nativeElement.querySelector('.close-btn') as HTMLButtonElement;
    expect(closeBtn).toBeTruthy();

    closeBtn.click();
    expect(component.closed.emit).toHaveBeenCalled();
  });
});
