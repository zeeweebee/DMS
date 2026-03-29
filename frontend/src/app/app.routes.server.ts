import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  { path: 'login',        renderMode: RenderMode.Prerender },
  { path: 'hello',        renderMode: RenderMode.Client },
  { path: 'models',       renderMode: RenderMode.Client },
  { path: 'stock',        renderMode: RenderMode.Client },
  { path: 'dealers',      renderMode: RenderMode.Client },
  { path: 'enquiries',    renderMode: RenderMode.Client },
  { path: 'bookings',     renderMode: RenderMode.Client },
  { path: 'sales',        renderMode: RenderMode.Client },
  { path: '**',           renderMode: RenderMode.Client }
];
