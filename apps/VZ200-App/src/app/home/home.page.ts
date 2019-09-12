import { Component, ViewChild } from '@angular/core';
import { RESTserviceService } from '../services/restservice.service';

@Component({
  selector: 'app-home',
  templateUrl: 'home.page.html',
  styleUrls: ['home.page.scss'],
})
export class HomePage {

  @ViewChild('reset', {static: false})
  buttonReset: HTMLButtonElement;

  constructor(public restService: RESTserviceService) {}

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
}
