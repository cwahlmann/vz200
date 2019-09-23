import { async, ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { IonicModule, IonRange } from '@ionic/angular';

import { HomePage } from './home.page';
import { HttpTestingController } from '@angular/common/http/testing';
import { HttpClient } from '@angular/common/http';

describe('HomePage', () => {
  let component: any;
  let fixture: ComponentFixture<HomePage>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [HomePage, HTMLIonRangeElement],
      imports: [IonicModule.forRoot()],
      providers: [{provide: HttpClient, class: HttpTestingController}]
    }).compileComponents();

    fixture = TestBed.createComponent(HomePage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have an emulator reset button with a click handler calling RESTservice', fakeAsync(() => {
    const buttonReset = fixture.nativeElement.querySelector('ion-button#reset');
    expect(buttonReset).toBeDefined();
    spyOn(component, 'onReset');
    buttonReset.click();
    tick();
    expect(component.onReset).toHaveBeenCalled();
  }));

  fit('should have an emulator volume range slider with an event handler calling RESTservice', fakeAsync(() => {
    const rangeVolume: IonRange = fixture.nativeElement.querySelector('ion-range#volume');
    expect(rangeVolume).toBeDefined();
    spyOn(component, 'onVolumeChanged');
    rangeVolume
    tick();
    expect(component.onVolumeChanged).toHaveBeenCalled();
  }));

});
