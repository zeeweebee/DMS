import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Dealers } from './dealers';

describe('Dealers', () => {
  let component: Dealers;
  let fixture: ComponentFixture<Dealers>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Dealers],
    }).compileComponents();

    fixture = TestBed.createComponent(Dealers);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
