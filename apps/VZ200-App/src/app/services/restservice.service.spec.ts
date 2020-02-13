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
    httpMock.verify();
  }));

  it('should issue a sound post request to the emulator', fakeAsync(() => {
    service.doVolumeChange(64).then(result => {
      expect(result).toEqual('Ok.');
    });
    const mockRequest = httpMock.expectOne(`${service.ROOT_URL}/sound/64`);
    expect(mockRequest.request.method).toBe('POST');
    mockRequest.flush('Ok.');
    httpMock.verify();
  }));

  it('should read the current volume from the emulator', fakeAsync(() => {
    service.getVolume().then(result => {
      expect(result).toEqual(64);
    });
    const mockRequest = httpMock.expectOne(`${service.ROOT_URL}/sound`);
    expect(mockRequest.request.method).toBe('GET');
    mockRequest.flush(64);
    httpMock.verify();
  }));

  it('should return the VZs Z80 registers', fakeAsync(() => {
    const expectedResult = {
      "AF": "5F0C",
      "AF'": "0000",
      "BC": "025F",
      "BC'": "0000",
      "DE": "0028",
      "DE'": "0000",
      "HL": "7839",
      "HL'": "0000",
      "I": "0000",
      "IX": "0000",
      "IY": "0000",
      "PC": "343B",
      "R": "0046",
      "SP": "FFBB"
    };
    service.getRegisters().then(registers => {
      expect(registers).toEqual(expectedResult);
    });
    const mockRequest = httpMock.expectOne(`${service.ROOT_URL}/registers`);
    expect(mockRequest.request.method).toBe('GET');
    mockRequest.flush(expectedResult);
    httpMock.verify();
  }));

  it('should upload a basic program', fakeAsync(() => {
    const HELLOWORLD = '10 PRINT"HELLO WORLD!"';
    service.uploadBasic(HELLOWORLD).then(result => {
      expect(result).toEqual('Daten eingespielt.');
    });
    const mockRequest = httpMock.expectOne(`${service.ROOT_URL}/bas`);
    expect(mockRequest.request.method).toBe('POST');
    mockRequest.flush('Daten eingespielt.', { headers: { 'Content-Type': 'application/octet-stream' } });
    httpMock.verify();
  }));

  it('should download hello world as vz program', fakeAsync(() => {
    const HELLOWORLD = '"HELLO WORLD!"';
    service.downloadVZ().then(result => {
      expect(result).toContain(HELLOWORLD);
    });
    const mockRequest = httpMock.expectOne(`${service.ROOT_URL}/vz?autorun=True`);
    expect(mockRequest.request.method).toBe('GET');
    mockRequest.flush('VZF0VZ200\n"HELLO WORLD!"', { headers: { 'Accept': 'application/octet-stream' } });
    httpMock.verify();
  }));

  it('should download hello world as basic source code', fakeAsync(() => {
    const HELLOWORLD = '10 PRINT"HELLO WORLD!"';
    service.downloadBasic().then(result => {
      expect(result).toContain(HELLOWORLD);
    });
    const mockRequest = httpMock.expectOne(`${service.ROOT_URL}/bas`);
    expect(mockRequest.request.method).toBe('GET');
    mockRequest.flush(HELLOWORLD, { headers: { 'Accept': 'application/octet-stream' } });
    httpMock.verify();
  }));

  it('should type text into the VZs command console', fakeAsync(() => {
    const HELLOWORLD = 'LIST\n';
    service.typeText(HELLOWORLD).then(result => {
      expect(result).toContain('Text getippt.');
    });
    const mockRequest = httpMock.expectOne(`${service.ROOT_URL}/typetext`);
    expect(mockRequest.request.method).toBe('POST');
    mockRequest.flush('Text getippt.', { headers: { 'Content-type': 'application/octet-stream' } });
    httpMock.verify();
  }));

});
