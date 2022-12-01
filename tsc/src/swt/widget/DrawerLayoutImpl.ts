// start - imports

	
import CommandAttr from '../../widget/CommandAttr';
import IWidget from '../../widget/IWidget';
import ILayoutParam from '../../widget/ILayoutParam';
import {plainToClass, Type, Exclude, Expose, Transform} from "class-transformer";
import 'babel-polyfill';
import {Gravity} from '../../widget/TypeConstants';
import {ITranform, TransformerFactory} from '../../widget/TransformerFactory';
import {Event} from '../../app/Event';
import {MotionEvent} from '../../app/MotionEvent';
import {DragEvent} from '../../app/DragEvent';
import {KeyEvent} from '../../app/KeyEvent';



import {ViewGroupImpl_LayoutParams} from './ViewGroupImpl';

// end - imports
import {ViewGroupImpl} from './ViewGroupImpl';
export abstract class DrawerLayoutImpl<T> extends ViewGroupImpl<T>{
	//start - body
	static initialize() {
    }	
	@Type(() => CommandAttr)
	@Expose({ name: "openDrawer" })
	openDrawer_!:CommandAttr<Gravity[]>| undefined;

	@Exclude()
	protected thisPointer: T;	
	protected abstract getThisPointer(): T;
	reset() : T {	
		super.reset();
		this.openDrawer_ = undefined;
		return this.thisPointer;
	}
	constructor(id: string, path: string[], event:  string) {
		super(id, path, event);
		this.thisPointer = this.getThisPointer();
	}
	

	public openDrawer(...value : Gravity[]) : T {
		if (this.openDrawer_ == null || this.openDrawer_ == undefined) {
			this.openDrawer_ = new CommandAttr<Gravity[]>();
		}
		
		this.openDrawer_.setSetter(true);
		this.openDrawer_.setValue(value);
		this.orderSet++;
		this.openDrawer_.setOrderSet(this.orderSet);
this.openDrawer_.setTransformer('gravity');		return this.thisPointer;
	}
	//end - body

}
	
//start - staticinit

export class DrawerLayout extends DrawerLayoutImpl<DrawerLayout> implements IWidget{
    getThisPointer(): DrawerLayout {
        return this;
    }
    
   	public getClass() {
		return DrawerLayout;
	}
	
   	constructor(id: string, path: string[], event: string) {
		super(id, path, event);	
	}
}

DrawerLayoutImpl.initialize();

//end - staticinit
