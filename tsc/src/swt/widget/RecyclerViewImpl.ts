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
export abstract class RecyclerViewImpl<T> extends ViewGroupImpl<T>{
	//start - body
	static initialize() {
    }	
	@Type(() => CommandAttr)
	@Expose({ name: "layoutManager" })
	layoutManager!:CommandAttr<string>| undefined;
	@Type(() => CommandAttr)
	@Expose({ name: "viewHolderIds" })
	viewHolderIds!:CommandAttr<string>| undefined;

	@Exclude()
	protected thisPointer: T;	
	protected abstract getThisPointer(): T;
	reset() : T {	
		super.reset();
		this.layoutManager = undefined;
		this.viewHolderIds = undefined;
		return this.thisPointer;
	}
	constructor(id: string, path: string[], event:  string) {
		super(id, path, event);
		this.thisPointer = this.getThisPointer();
	}
	

	public setLayoutManager(value : string) : T {
		if (this.layoutManager == null || this.layoutManager == undefined) {
			this.layoutManager = new CommandAttr<string>();
		}
		
		this.layoutManager.setSetter(true);
		this.layoutManager.setValue(value);
		this.orderSet++;
		this.layoutManager.setOrderSet(this.orderSet);
		return this.thisPointer;
	}

	public setViewHolderIds(value : string) : T {
		if (this.viewHolderIds == null || this.viewHolderIds == undefined) {
			this.viewHolderIds = new CommandAttr<string>();
		}
		
		this.viewHolderIds.setSetter(true);
		this.viewHolderIds.setValue(value);
		this.orderSet++;
		this.viewHolderIds.setOrderSet(this.orderSet);
		return this.thisPointer;
	}
	//end - body

}
	
//start - staticinit

export class RecyclerView extends RecyclerViewImpl<RecyclerView> implements IWidget{
    getThisPointer(): RecyclerView {
        return this;
    }
    
   	public getClass() {
		return RecyclerView;
	}
	
   	constructor(id: string, path: string[], event: string) {
		super(id, path, event);	
	}
}

RecyclerViewImpl.initialize();

//end - staticinit
