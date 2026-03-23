import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DealerListComponent } from './dealer-list';

describe('DealerListComponent', () => {
  let component: DealerListComponent;
  let fixture: ComponentFixture<DealerListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DealerListComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(DealerListComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
