import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class RESTserviceService {

  public ROOT_URL = 'http://localhost:8080/vz200';
  constructor(private http: HttpClient) { }

  /**
   * Reset the emulator
   */
  public doReset(): Promise<string> { 
    return this.http.post(`${this.ROOT_URL}/reset`, null, {responseType: "text"} ).toPromise();
  }

  /**
   * Sets the sound volume on the emulator
   * @param volume to set (0-255)
   */
  public doVolumeChange(volume: number): Promise<string> { 
    return this.http.post(`${this.ROOT_URL}/sound/${volume}`, null, {responseType: "text"} ).toPromise();
  }

  /**
   * Reads the sound volume
   * @returns 0-255
   */
  public async getVolume(): Promise<number> { 
    const result = await this.http.get(`${this.ROOT_URL}/sound`).toPromise();
    return result as number;
  }

  /**
   * Reads and returns the Z80 registers of the emulated CPU
   */
  public async getRegisters(): Promise<Object> {
    return await this.http.get(`${this.ROOT_URL}/registers`).toPromise();
  }

  /**
   * Uploads Basic Code to the Emulator
   * @param code Basic Code to upload
   */
  public uploadBasic(code: string): Promise<string> { 
    return this.http.post(`${this.ROOT_URL}/bas`, code, {responseType: "text"} ).toPromise();
  }

  /**
   * Download the current BASIC Program in VZ binary format
   * @returns Uint8Array
   */
  public async downloadVZ(): Promise<Uint8Array> {
    const result = await this.http.get(`${this.ROOT_URL}/vz?autorun=True`).toPromise();
    return result as Uint8Array;
  }

  public async downloadBasic(): Promise<String> {
    const result = await this.http.get(`${this.ROOT_URL}/printer/flush`).toPromise();
    return result as String;
  }

}
