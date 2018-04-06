import {
  Component,
  OnInit
} from '@angular/core';
import { CordovaService } from "../../common/services/cordova.service";
import {UIConstants} from '../../common/ui/ui-constants';
import {Router} from '@angular/router';

@Component({
  selector: 'app-startup',
  template: ''
})
export class StartupComponent {
    constructor(private cordova : CordovaService,private router:Router) {

        console.log("CONSTRUCTOR StartupComponent");

        if (this.cordova.isRunningCordova()){

            // wait until cordova device init is ready
            this.cordova.subscribeServiceReady().subscribe(()=>{

                // per default go to app
                this.router.navigate(['app']);

                // when new share contet - go to share screen
                this.cordova.onNewShareContent().subscribe(
                   (data:any) => {
                    // TODO: take URI and processes on share screen
                    // this.router.navigate(['share', URI]);
                    this.router.navigate(['app','share'],{queryParams:data});
                }, (error) => {
                    console.log("ERROR on new share event",error);
                });

            });

        }
        else{
            this.router.navigate([UIConstants.ROUTER_PREFIX+'login']);
        }
    }
}
