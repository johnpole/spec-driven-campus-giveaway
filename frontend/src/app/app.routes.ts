import { Routes } from '@angular/router';
import { FeedComponent } from './features/feed/feed.component';
import { ItemDetailComponent } from './features/item-detail/item-detail.component';
import { PostItemComponent } from './features/post-item/post-item.component';
import { MyActivityComponent } from './features/my-activity/my-activity.component';

export const routes: Routes = [
  {
    path: '',
    component: FeedComponent
  },
  {
    path: 'browse',
    redirectTo: '',
    pathMatch: 'full'
  },
  {
    path: 'items/:id',
    component: ItemDetailComponent
  },
  {
    path: 'post',
    component: PostItemComponent
  },
  {
    path: 'my-posts',
    component: MyActivityComponent
  },
  {
    path: 'my-activity',
    component: MyActivityComponent
  }
];
