import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class RESTserviceService {

  public ROOT_URL = 'http://localhost:8080/vz200';
  constructor(private http: HttpClient) { }

  public doReset(): Promise<Object> {
    return this.http.post(`${this.ROOT_URL}/reset`, '{}' ).toPromise();
  }
}
