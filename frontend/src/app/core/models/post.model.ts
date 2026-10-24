export type Category =
  | 'Furniture'
  | 'Electronics'
  | 'Textbooks'
  | 'Kitchen'
  | 'Dorm essentials'
  | 'Clothing'
  | 'Bikes'
  | 'Other';

export const CATEGORIES: readonly Category[] = [
  'Furniture',
  'Electronics',
  'Textbooks',
  'Kitchen',
  'Dorm essentials',
  'Clothing',
  'Bikes',
  'Other'
] as const;

export type Condition = 'LIKE_NEW' | 'GOOD' | 'FAIR';

export type PostStatus = 'AVAILABLE' | 'CLAIMED' | 'TAKEN' | 'ARCHIVED';

export interface CreatePostRequest {
  title: string;
  description: string;
  category: Category;
  condition: Condition;
  price: number;
  pickupLocation: string;
  availableUntil: string; // ISO-8601 UTC string
  photoUrl: string;
  posterName: string;
  posterContact: string;
  legalConfirmed: boolean;
}

export interface CreatePostResponse {
  id: string;
  editToken: string;
}

export interface PostSummary {
  id: string;
  title: string;
  category: Category;
  condition: Condition;
  price: number;
  isFree: boolean;
  pickupLocation: string;
  availableUntil: string;
  photoUrl: string;
  interestCount: number;
  createdAt: string;
  status: PostStatus;
}

export interface PostDetail {
  id: string;
  title: string;
  description: string;
  category: Category;
  condition: Condition;
  price: number;
  isFree: boolean;
  pickupLocation: string;
  availableUntil: string;
  photoUrl: string;
  status: PostStatus;
  posterName: string;
  interestCount: number;
  createdAt: string;
  /** Set for signed-in viewers: they posted this item (so they can't request it). */
  ownedByViewer?: boolean;
  /** Set for signed-in viewers: they already sent a request for this item. */
  requestedByViewer?: boolean;
}

export interface UpdateStatusRequest {
  status: PostStatus;
}
