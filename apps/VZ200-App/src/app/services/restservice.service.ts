import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class RESTserviceService {

  public ROOT_URL = 'http://localhost:8080/vz200';
  constructor(private http: HttpClient) { }

  public doReset(): Promise<string> { 
    return this.http.post(`${this.ROOT_URL}/reset`, null, {responseType: "text"} ).toPromise();
  }

  public doVolumeChange(volume: number): Promise<string> { 
    return this.http.post(`${this.ROOT_URL}/sound/${volume}`, null, {responseType: "text"} ).toPromise();
  }

  public async getVolume(): Promise<number> { 
    const result = await this.http.get(`${this.ROOT_URL}/sound`).toPromise();
    return result as number;
  }
}
