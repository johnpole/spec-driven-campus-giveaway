import {
  ChangeDetectionStrategy,
  Component,
  computed,
  input,
  output,
  signal
} from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { PostDetail } from '../../core/models/post.model';

@Component({
  selector: 'app-flier-modal',
  standalone: true,
  imports: [DecimalPipe],
  template: `
    <div
      class="flier-backdrop"
      [class.open]="isOpen()"
      (click)="onBackdropClick()"
      role="dialog"
      aria-modal="true"
      aria-labelledby="flier-dialog-title">
      <div
        class="flier-modal-container"
        [class.open]="isOpen()"
        (click)="$event.stopPropagation()">
        <!-- Modal Toolbar (Hidden during print) -->
        <div class="modal-toolbar no-print">
          <div class="toolbar-title">
            <span class="material-symbols-outlined">print</span>
            <h3 id="flier-dialog-title">Printable Campus Flier</h3>
          </div>
          <div class="toolbar-actions">
            <button type="button" class="print-btn" (click)="triggerPrint()">
              <span class="material-symbols-outlined">print</span>
              <span>Print Flier</span>
            </button>
            <button
              type="button"
              class="close-btn"
              (click)="closeModal()"
              aria-label="Close modal">
              <span class="material-symbols-outlined">close</span>
            </button>
          </div>
        </div>

        <!-- Printable Flier Card (Prints cleanly on 4x6 or Letter) -->
        <div class="flier-sheet" id="printable-flier-sheet">
          <!-- Top Header -->
          <div class="flier-header">
            <div class="rpi-brand">
              <span class="brand-tag">RPI</span>
              <span class="brand-title">CAMPUS GIVEAWAY</span>
            </div>
            <span class="sustainability-tag">PASS IT ON · REDUCE WASTE</span>
          </div>

          <!-- Main Offer Banner -->
          <div class="flier-main">
            <div class="price-banner" [class.is-free]="post()?.isFree || post()?.price === 0">
              @if (post()?.isFree || post()?.price === 0) {
                <span>FREE GEAR</span>
              } @else {
                <span>&#36;{{ post()?.price }}</span>
              }
            </div>

            <h1 class="item-title">{{ post()?.title }}</h1>
            <p class="item-desc">{{ post()?.description }}</p>

            <div class="item-meta-row">
              <div class="meta-block">
                <span class="meta-label">PICKUP AT</span>
                <span class="meta-val">{{ post()?.pickupLocation }}</span>
              </div>
              <div class="meta-block">
                <span class="meta-label">CATEGORY</span>
                <span class="meta-val">{{ post()?.category }}</span>
              </div>
            </div>
          </div>

          <!-- QR Code Section -->
          <div class="qr-section">
            <div class="qr-box">
              <img
                [src]="qrCodeUrl()"
                alt="QR Code to claim item"
                class="qr-image"
                (error)="onQrError()" />
            </div>
            <div class="qr-instructions">
              <p class="scan-heading">Scan with phone camera</p>
              <p class="scan-subtext">Directly contact seller & claim this item on Campus Giveaway</p>
              <span class="short-url">{{ itemUrl() }}</span>
            </div>
          </div>

          <!-- Tear-off style bottom slips -->
          <div class="tear-slips-container">
            <div class="tear-line">
              <span>✂ - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - ✂</span>
            </div>
            <div class="slips-row">
              @for (slip of [1, 2, 3, 4]; track slip) {
                <div class="slip">
                  <span class="slip-item">{{ post()?.title }}</span>
                  <span class="slip-price">{{ slipPrice() }}</span>
                  <span class="slip-loc">{{ post()?.pickupLocation }}</span>
                  <span class="slip-qr">Scan QR above</span>
                </div>
              }
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .flier-backdrop {
      position: fixed;
      inset: 0;
      background: rgba(0, 0, 0, 0.6);
      backdrop-filter: blur(4px);
      -webkit-backdrop-filter: blur(4px);
      z-index: 120;
      opacity: 0;
      visibility: hidden;
      transition: opacity 0.25s ease, visibility 0.25s ease;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 16px;

      &.open {
        opacity: 1;
        visibility: visible;
      }
    }

    .flier-modal-container {
      background: #f3f4f6;
      border-radius: 16px;
      width: 100%;
      max-width: 440px;
      max-height: 90vh;
      display: flex;
      flex-direction: column;
      overflow: hidden;
      box-shadow: 0 20px 40px rgba(0, 0, 0, 0.25);
    }

    .modal-toolbar {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 12px 16px;
      background: #ffffff;
      border-bottom: 1px solid #e5e7eb;

      .toolbar-title {
        display: flex;
        align-items: center;
        gap: 8px;
        color: #111827;

        h3 {
          font-size: 15px;
          font-weight: 700;
          margin: 0;
        }
      }

      .toolbar-actions {
        display: flex;
        align-items: center;
        gap: 8px;

        .print-btn {
          display: inline-flex;
          align-items: center;
          gap: 6px;
          background: #e11d48;
          color: #ffffff;
          border: none;
          padding: 8px 14px;
          border-radius: 8px;
          font-size: 13px;
          font-weight: 600;
          cursor: pointer;
          min-height: 38px;

          &:hover {
            background: #be123c;
          }
        }

        .close-btn {
          background: #f3f4f6;
          border: none;
          border-radius: 50%;
          width: 34px;
          height: 34px;
          display: flex;
          align-items: center;
          justify-content: center;
          color: #4b5563;
          cursor: pointer;

          &:hover {
            background: #e5e7eb;
          }
        }
      }
    }

    .flier-sheet {
      background: #ffffff;
      padding: 20px;
      margin: 12px;
      border: 2px solid #111827;
      border-radius: 8px;
      overflow-y: auto;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);

      .flier-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        border-bottom: 2px solid #111827;
        padding-bottom: 8px;
        margin-bottom: 12px;

        .rpi-brand {
          display: flex;
          align-items: center;
          gap: 6px;

          .brand-tag {
            background: #e11d48;
            color: #ffffff;
            font-weight: 900;
            font-size: 13px;
            padding: 3px 6px;
            border-radius: 4px;
          }

          .brand-title {
            font-weight: 800;
            font-size: 13px;
            letter-spacing: 0.5px;
            color: #111827;
          }
        }

        .sustainability-tag {
          font-size: 9px;
          font-weight: 700;
          color: #166534;
          letter-spacing: 0.5px;
        }
      }

      .flier-main {
        text-align: center;
        margin-bottom: 14px;

        .price-banner {
          display: inline-block;
          background: #111827;
          color: #ffffff;
          font-size: 20px;
          font-weight: 900;
          padding: 6px 18px;
          border-radius: 6px;
          margin-bottom: 8px;
          letter-spacing: 1px;

          &.is-free {
            background: #16a34a;
          }
        }

        .item-title {
          font-size: 22px;
          font-weight: 900;
          color: #111827;
          margin: 0 0 6px 0;
          line-height: 1.2;
        }

        .item-desc {
          font-size: 12px;
          color: #4b5563;
          margin: 0 0 10px 0;
          line-height: 1.4;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          overflow: hidden;
        }

        .item-meta-row {
          display: flex;
          justify-content: center;
          gap: 20px;
          border-top: 1px dashed #d1d5db;
          border-bottom: 1px dashed #d1d5db;
          padding: 6px 0;

          .meta-block {
            display: flex;
            flex-direction: column;

            .meta-label {
              font-size: 9px;
              font-weight: 700;
              color: #6b7280;
            }

            .meta-val {
              font-size: 12px;
              font-weight: 700;
              color: #111827;
            }
          }
        }
      }

      .qr-section {
        display: flex;
        align-items: center;
        gap: 12px;
        background: #f9fafb;
        border: 1.5px solid #e5e7eb;
        border-radius: 8px;
        padding: 10px;
        margin-bottom: 14px;

        .qr-box {
          width: 90px;
          height: 90px;
          flex-shrink: 0;
          background: #ffffff;
          padding: 4px;
          border: 1px solid #d1d5db;
          border-radius: 6px;

          .qr-image {
            width: 100%;
            height: 100%;
            object-fit: contain;
          }
        }

        .qr-instructions {
          flex: 1;

          .scan-heading {
            font-size: 13px;
            font-weight: 800;
            color: #111827;
            margin: 0 0 2px 0;
          }

          .scan-subtext {
            font-size: 11px;
            color: #4b5563;
            margin: 0 0 4px 0;
            line-height: 1.3;
          }

          .short-url {
            font-size: 10px;
            font-family: monospace;
            color: #e11d48;
            word-break: break-all;
          }
        }
      }

      .tear-slips-container {
        .tear-line {
          text-align: center;
          font-size: 10px;
          color: #9ca3af;
          margin-bottom: 6px;
        }

        .slips-row {
          display: grid;
          grid-template-columns: repeat(4, 1fr);
          gap: 6px;

          .slip {
            border: 1px dashed #9ca3af;
            border-top: none;
            padding: 6px 4px;
            text-align: center;
            display: flex;
            flex-direction: column;
            gap: 2px;

            .slip-item {
              font-size: 9px;
              font-weight: 700;
              white-space: nowrap;
              overflow: hidden;
              text-overflow: ellipsis;
            }

            .slip-price {
              font-size: 10px;
              font-weight: 800;
              color: #e11d48;
            }

            .slip-loc {
              font-size: 8px;
              color: #6b7280;
              white-space: nowrap;
              overflow: hidden;
              text-overflow: ellipsis;
            }

            .slip-qr {
              font-size: 7px;
              color: #4b5563;
            }
          }
        }
      }
    }

    @media print {
      body * {
        visibility: hidden !important;
      }

      .flier-backdrop,
      .flier-modal-container,
      .flier-sheet,
      .flier-sheet * {
        visibility: visible !important;
      }

      .no-print {
        display: none !important;
      }

      .flier-backdrop {
        position: static !important;
        background: none !important;
        padding: 0 !important;
      }

      .flier-modal-container {
        box-shadow: none !important;
        max-width: 100% !important;
      }

      .flier-sheet {
        margin: 0 !important;
        border: 2px solid #000 !important;
        page-break-inside: avoid;
      }
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class FlierModalComponent {
  readonly isOpen = input<boolean>(false);
  readonly post = input<PostDetail | null>(null);

  readonly closed = output<void>();
  readonly qrLoadFailed = signal<boolean>(false);

  readonly slipPrice = computed(() => {
    const p = this.post();
    if (!p || p.isFree || p.price === 0) return 'FREE';
    return '$' + p.price;
  });

  readonly itemUrl = computed(() => {
    const p = this.post();
    if (!p) return 'https://giveaway.rpi.edu';
    if (typeof window !== 'undefined' && window.location) {
      return `${window.location.origin}/items/${p.id}`;
    }
    return `https://giveaway.rpi.edu/items/${p.id}`;
  });

  readonly qrCodeUrl = computed(() => {
    if (this.qrLoadFailed()) {
      return 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="100" height="100" viewBox="0 0 100 100"><rect width="100" height="100" fill="%23fff"/><rect x="10" y="10" width="30" height="30" fill="%23000"/><rect x="15" y="15" width="20" height="20" fill="%23fff"/><rect x="20" y="20" width="10" height="10" fill="%23000"/><rect x="60" y="10" width="30" height="30" fill="%23000"/><rect x="65" y="15" width="20" height="20" fill="%23fff"/><rect x="70" y="20" width="10" height="10" fill="%23000"/><rect x="10" y="60" width="30" height="30" fill="%23000"/><rect x="15" y="65" width="20" height="20" fill="%23fff"/><rect x="20" y="70" width="10" height="10" fill="%23000"/><rect x="50" y="50" width="15" height="15" fill="%23000"/><rect x="75" y="65" width="15" height="25" fill="%23000"/></svg>';
    }
    return `https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=${encodeURIComponent(this.itemUrl())}`;
  });

  onBackdropClick(): void {
    this.closeModal();
  }

  closeModal(): void {
    this.closed.emit();
  }

  onQrError(): void {
    this.qrLoadFailed.set(true);
  }

  triggerPrint(): void {
    if (typeof window !== 'undefined') {
      window.print();
    }
  }
}
