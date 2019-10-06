import { TestBed, getTestBed, fakeAsync, tick } from '@angular/core/testing';

import { RESTserviceService } from './restservice.service';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

describe('RESTserviceService', () => {
  let service: any;
  let httpMock: HttpTestingController;
  let injector: TestBed;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [RESTserviceService]
    });
    injector = getTestBed();
    service = injector.get(RESTserviceService);
    httpMock = injector.get(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should inject http service', () => {
    expect(service.http).toBeDefined();
  });

  it('should issue a reset command to the emulator', fakeAsync(() => {
    service.doReset().then(result => {
      expect(result).toEqual('reset done');
    });
    const mockRequest = httpMock.expectOne(`${service.ROOT_URL}/reset`);
    expect(mockRequest.request.method).toBe('POST');
    mockRequest.flush('reset done');
  }));

  it('should issue a sound post request to the emulator', fakeAsync(() => {
    service.doVolumeChange(64).then(result => {
      expect(result).toEqual('Ok.');
    });
    const mockRequest = httpMock.expectOne(`${service.ROOT_URL}/sound/64`);
    expect(mockRequest.request.method).toBe('POST');
    mockRequest.flush('Ok.');
  }));

  it('should read the current volume from the emulator', fakeAsync(() => {
    service.getVolume().then(result => {
      expect(result).toEqual(64);
    });
    const mockRequest = httpMock.expectOne(`${service.ROOT_URL}/sound`);
    expect(mockRequest.request.method).toBe('GET');
    mockRequest.flush(64);
  }));

});
