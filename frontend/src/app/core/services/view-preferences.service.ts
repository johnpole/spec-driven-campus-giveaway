import { Injectable, effect, signal } from '@angular/core';

export type FeedLayout = 'grid' | 'list';
export type FeedDensity = 'comfortable' | 'compact';
export type FeedSort = 'newest' | 'free-first' | 'price-low';

interface ViewPreferences {
  layout: FeedLayout;
  density: FeedDensity;
  sort: FeedSort;
}

const STORAGE_KEY = 'cg_view_prefs';
const DEFAULTS: ViewPreferences = { layout: 'grid', density: 'comfortable', sort: 'newest' };

/** How each student likes to browse the feed. Remembered on this device only. */
@Injectable({ providedIn: 'root' })
export class ViewPreferencesService {
  private readonly initial = this.load();

  readonly layout = signal<FeedLayout>(this.initial.layout);
  readonly density = signal<FeedDensity>(this.initial.density);
  readonly sort = signal<FeedSort>(this.initial.sort);

  constructor() {
    effect(() => this.save({ layout: this.layout(), density: this.density(), sort: this.sort() }));
  }

  reset(): void {
    this.layout.set(DEFAULTS.layout);
    this.density.set(DEFAULTS.density);
    this.sort.set(DEFAULTS.sort);
  }

  private load(): ViewPreferences {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      return raw ? { ...DEFAULTS, ...JSON.parse(raw) } : DEFAULTS;
    } catch {
      return DEFAULTS;
    }
  }

  private save(prefs: ViewPreferences): void {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(prefs));
    } catch {
      // Private mode or blocked storage: preferences simply aren't remembered.
    }
  }
}
