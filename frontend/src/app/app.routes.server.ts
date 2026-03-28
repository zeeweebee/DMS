import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  // Only the login page can be prerendered (no auth needed, no localStorage)
  { path: 'login', renderMode: RenderMode.Prerender },

  // All other routes require auth (localStorage) — must render on the client
  { path: '**', renderMode: RenderMode.Client }
];
