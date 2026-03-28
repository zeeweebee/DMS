import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  // Only the login page can be prerendered (no auth needed, no localStorage)
  { path: 'login', renderMode: RenderMode.Prerender },

  // All other routes require auth (localStorage) — must render on the client
  { path: 'hello', renderMode: RenderMode.Client },
  { path: 'models', renderMode: RenderMode.Client },
  { path: 'stock', renderMode: RenderMode.Client },
  { path: 'dealers', renderMode: RenderMode.Client },
  { path: '**', renderMode: RenderMode.Client }
];