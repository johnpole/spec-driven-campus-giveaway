export interface UserProfile {
  id: string;
  email: string;
  name: string;
  firstName?: string;
  lastName?: string;
  phoneNumber?: string;
  pictureUrl?: string;
  isAdmin?: boolean;
}

export interface AuthResponse {
  token: string;
  user: UserProfile;
}

/** One message in the pickup conversation between poster (SELLER) and requester (BUYER). */
export interface InterestReply {
  id: string;
  authorName: string;
  authorRole: 'SELLER' | 'BUYER';
  message: string;
  createdAt: string;
}

/** A request on one of my posts, as the API returns it (GET /api/users/me/posts → inquiries). */
export interface UserInquirySummary {
  id: string;
  visitorName: string;
  visitorContact: string;
  message: string;
  createdAt: string;
  pickupAt: string | null;
  pickupPlace: string | null;
  replies: InterestReply[];
}

export interface UserPostItem {
  id: string;
  title: string;
  category: string;
  price: number;
  isFree: boolean;
  status: string;
  rejectionReason?: string | null;
  photoUrl: string;
  pickupLocation?: string;
  createdAt: string;
  interestCount: number;
  inquiries: UserInquirySummary[];
}

/** A request I sent, as the API returns it (GET /api/users/me/interests). */
export interface UserInquiryItem {
  id: string;
  postId: string;
  itemTitle: string;
  itemPhotoUrl: string;
  itemStatus: string;
  message: string;
  createdAt: string;
  sellerName: string | null;
  pickupAt: string | null;
  pickupPlace: string | null;
  replies: InterestReply[];
}
