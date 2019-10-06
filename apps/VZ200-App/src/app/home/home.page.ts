import { Component, ViewChild, DebugElement, OnInit } from '@angular/core';
import { RESTserviceService } from '../services/restservice.service';

@Component({
  selector: 'app-home',
  templateUrl: 'home.page.html',
  styleUrls: ['home.page.scss']
})
export class HomePage{

  @ViewChild('reset', {static: false})
  buttonReset: HTMLButtonElement;

  @ViewChild('volume', {static: false})
  rangeVolume: HTMLInputElement;

  public soundVolume: number;

  constructor(public restService: RESTserviceService) {}

  async ionViewDidEnter(){
    console.log('ionViewDidEnter() called.');
    this.soundVolume = await this.restService.getVolume();
  }

  public async onReset(){
    this.buttonReset.disabled = true;
    try {
      const result = await this.restService.doReset();
      console.log('onReset: result = ' + result);
    } catch (error) {
      console.error('onReset error: ' + JSON.stringify(error));
    } finally {
      this.buttonReset.disabled = false;
    }
  }

  public async onVolumeChanged($event){
    try {
      console.log($event);
      const result = await this.restService.doVolumeChange($event.detail.value);
      this.soundVolume = $event.detail.value;
      console.log('onChangeVolume: result = ' + result);
    } catch (error) {
      console.error('onChangeVolume error: ' + JSON.stringify(error));
    }
  }
}
