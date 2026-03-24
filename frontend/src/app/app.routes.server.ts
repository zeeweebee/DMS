import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  { path: '',          renderMode: RenderMode.Prerender },
  { path: 'login',     renderMode: RenderMode.Prerender },
  { path: 'hello',     renderMode: RenderMode.Prerender },
  { path: 'models',    renderMode: RenderMode.Prerender },
  { path: 'models/add', renderMode: RenderMode.Prerender },
  { path: 'models/edit/:id', renderMode: RenderMode.Server },
  { path: 'stock',     renderMode: RenderMode.Server },
  { path: 'stock/add', renderMode: RenderMode.Server },
  { path: 'add',       renderMode: RenderMode.Prerender },
  { path: 'edit/:id',  renderMode: RenderMode.Server },
  { path: '**',        renderMode: RenderMode.Server }
];