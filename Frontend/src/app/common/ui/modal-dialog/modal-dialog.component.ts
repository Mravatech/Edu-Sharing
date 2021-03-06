import {
  Component, Input, Output, EventEmitter, OnInit, HostListener, ViewChild, ElementRef,
  QueryList
} from '@angular/core';
import {TranslateService} from "@ngx-translate/core";
import {trigger} from "@angular/animations";
import {UIAnimation} from "../ui-animation";

@Component({
  selector: 'modal-dialog',
  templateUrl: 'modal-dialog.component.html',
  styleUrls: ['modal-dialog.component.scss'],
  animations: [
    trigger('fade', UIAnimation.fade()),
    trigger('cardAnimation', UIAnimation.cardAnimation())
  ]
})
/**
 * An common edu-sharing modal dialog
 */
export class ModalDialogComponent{
  @HostListener('document:keydown', ['$event'])
  handleKeyboardEvent(event: KeyboardEvent) {
    if(event.code=="Escape" && this.isCancelable){
      event.preventDefault();
      event.stopPropagation();
      this.cancel();
      return;
    }
  }
  /**
   * priority, useful if the dialog seems not to be in the foreground
   * Values greater 0 will raise the z-index
   */
  @Input() priority = 0;
  /**
   * Wether or not this dialog can be closed using escape or the icon
   * @type {boolean}
   */
  @Input() isCancelable = false;
  /**
   * Allow the dialog to scroll it contents
   * @type {boolean}
   */
  @Input() isScrollable = false;
  /**
   * The title, will be translated automatically
   * The dialog will only be visible if the title is not null
   */
  @Input() title : string;
  /**
   * The message, will be translated automatically
   */
  @Input() message : string;
  /**
   * Additional dynamic content for your language string, use an object, e.g.
   * Language String: Hello {{ name }}
   * And use messageParameters={name:'World'}
   */
  @Input() messageParameters : any;
  /**
   * Will be emitted when the users cancels the dialog
   * @type {EventEmitter}
   */
  @Output() onCancel = new EventEmitter();
  /** A list of buttons, see @DialogButton
   * Also use the DialogButton.getYesNo() and others if applicable!
   */
  public _buttons : DialogButton[];
  @Input() set buttons (buttons :  DialogButton[]){
    if(!buttons){
      this._buttons=null;
      return;
    }
   this._buttons=buttons.reverse();
   setTimeout(()=> {
     if(this.buttonElements)
      this.buttonElements.nativeElement.focus();
   },10);
  }

  @ViewChild('buttonElements') buttonElements : ElementRef;

  public click(btn : DialogButton){
    btn.callback();
  }
  public cancel(){
    this.onCancel.emit();
  }
}

export class DialogButton {
  public static getOkCancel(cancel : Function,ok : Function) : DialogButton[]{
    return [
          new DialogButton("CANCEL",DialogButton.TYPE_CANCEL,cancel),
          new DialogButton("OK",DialogButton.TYPE_PRIMARY,ok),
    ];
  }
  public static getOk(ok : Function) : DialogButton[]{
    return DialogButton.getSingleButton("OK",ok);
  }
  public static getSingleButton(label:string,ok : Function) : DialogButton[]{
    return [
      new DialogButton(label,DialogButton.TYPE_PRIMARY,ok),
    ];
  }
  public static getYesNo(no : Function,yes : Function) : DialogButton[]{
    return [
      new DialogButton("NO",DialogButton.TYPE_CANCEL,no),
      new DialogButton("YES",DialogButton.TYPE_PRIMARY,yes),
    ];
  }
  public static getNextCancel(cancel : Function,next : Function) : DialogButton[]{
    return [
      new DialogButton("CANCEL",DialogButton.TYPE_CANCEL,cancel),
      new DialogButton("NEXT",DialogButton.TYPE_PRIMARY,next),
    ];
  }
  public static TYPE_PRIMARY=1;
  public static TYPE_CANCEL=2;
  /**
   * @param name the button name, which is used for the translation
   * @param type the button type, use one of the constants
   * @param callback A function callback when this option is choosen.
   */
  constructor(public name: string,public type : number, public callback: Function) {
  }

}
