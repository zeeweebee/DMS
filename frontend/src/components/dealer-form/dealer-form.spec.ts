import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DealerForm } from './dealer-form';

describe('DealerForm', () => {
  let component: DealerForm;
  let fixture: ComponentFixture<DealerForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DealerForm],
    }).compileComponents();

    fixture = TestBed.createComponent(DealerForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
