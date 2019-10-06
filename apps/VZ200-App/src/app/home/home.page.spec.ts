import { async, ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { IonicModule, IonRange } from '@ionic/angular';

import { HomePage } from './home.page';
import { HttpTestingController } from '@angular/common/http/testing';
import { HttpClient } from '@angular/common/http';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { By } from '@angular/platform-browser';
import { RESTserviceService } from '../services/restservice.service';
import { Mock } from 'ts-mocks';

let mockRestService: Mock<RESTserviceService>;

describe('HomePage', () => {
  let component: any;
  let fixture: ComponentFixture<HomePage>;

  beforeEach(async(() => {
    mockRestService = new Mock<RESTserviceService>({
      getVolume: () => Promise.resolve(128)
    })

    TestBed.configureTestingModule({
      declarations: [HomePage],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
      imports: [IonicModule.forRoot()],
      providers: [{provide: HttpClient, class: HttpTestingController}, 
        {provide: RESTserviceService, useFactory: () => mockRestService.Object}]
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

  xit('should have an emulator volume range slider with an event handler calling RESTservice', fakeAsync(() => {
    const rangeVolume = fixture.debugElement.query(By.css('ion-range#volume')).nativeElement;
    expect(rangeVolume).not.toBeNull();
    spyOn(component, 'onVolumeChanged');
    component.rangeVolume.focus();
    tick();
    expect(component.onVolumeChanged).toHaveBeenCalled();
  }));

  // JW Issue with mocking the service here (one timer still in queue) 
  xit('should read the emulators volume on start and set the slider to the correct position', fakeAsync(() => {
    // mockRestService.extend({getVolume: async () => Promise.resolve(128)});
    component.ionViewDidEnter();
    tick();
    fixture.detectChanges();
    expect(component.restService.getVolume).toHaveBeenCalled();
    expect(component.soundVolume).toEqual(128);
    expect(component.rangeVolume.value).toEqual(128);
  }));

});
