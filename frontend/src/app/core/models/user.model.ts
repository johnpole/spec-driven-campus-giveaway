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

export interface UserInquirySummary {
  id: string;
  inquirerName: string;
  inquirerEmail: string;
  message: string;
  createdAt: string;
}

export interface UserPostItem {
  id: string;
  title: string;
  category: string;
  price: number;
  isFree: boolean;
  status: string;
  photoUrl: string;
  createdAt: string;
  interestCount: number;
  inquiries: UserInquirySummary[];
}

export interface UserInquiryItem {
  interestId: string;
  postId: string;
  postTitle: string;
  postCategory: string;
  postPrice: number;
  postPhotoUrl: string;
  postStatus: string;
  message: string;
  createdAt: string;
}
