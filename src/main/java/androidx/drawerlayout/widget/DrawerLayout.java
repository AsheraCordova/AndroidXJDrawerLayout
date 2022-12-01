package androidx.drawerlayout.widget;
import r.android.graphics.Rect;
import r.android.graphics.drawable.Drawable;
import r.android.os.Build;
import r.android.view.Gravity;
import r.android.view.View;
import r.android.view.ViewGroup;
import r.android.view.accessibility.AccessibilityEvent;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import java.util.ArrayList;
import java.util.List;
public class DrawerLayout extends ViewGroup {
  private static final String TAG="DrawerLayout";
  public static final int STATE_IDLE=ViewDragHelper.STATE_IDLE;
  public static final int STATE_DRAGGING=ViewDragHelper.STATE_DRAGGING;
  public static final int STATE_SETTLING=ViewDragHelper.STATE_SETTLING;
  public static final int LOCK_MODE_UNLOCKED=0;
  public static final int LOCK_MODE_LOCKED_CLOSED=1;
  public static final int LOCK_MODE_LOCKED_OPEN=2;
  public static final int LOCK_MODE_UNDEFINED=3;
  private static final int MIN_DRAWER_MARGIN=64;
  private static final int DEFAULT_SCRIM_COLOR=0x99000000;
  private static final int PEEK_DELAY=160;
  private static final int MIN_FLING_VELOCITY=400;
  private static final boolean ALLOW_EDGE_LOCK=false;
  private static final boolean CHILDREN_DISALLOW_INTERCEPT=true;
  private static final float TOUCH_SLOP_SENSITIVITY=1.f;
  static final boolean CAN_HIDE_DESCENDANTS=Build.VERSION.SDK_INT >= 19;
  private static final boolean SET_DRAWER_SHADOW_FROM_ELEVATION=Build.VERSION.SDK_INT >= 21;
  private float mDrawerElevation;
  private final int mMinDrawerMargin;
  private int mScrimColor=DEFAULT_SCRIM_COLOR;
  private float mScrimOpacity;
  private final ViewDragHelper mLeftDragger;
  private final ViewDragHelper mRightDragger;
  private int mDrawerState;
  private boolean mInLayout;
  private boolean mFirstLayout=true;
  private int mLockModeLeft=LOCK_MODE_UNDEFINED;
  private int mLockModeRight=LOCK_MODE_UNDEFINED;
  private int mLockModeStart=LOCK_MODE_UNDEFINED;
  private int mLockModeEnd=LOCK_MODE_UNDEFINED;
  private boolean mChildrenCanceledTouch;
  private DrawerListener mListener;
  private List<DrawerListener> mListeners;
  private float mInitialMotionX;
  private float mInitialMotionY;
  private WindowInsetsCompat mLastInsets;
  private boolean mDrawStatusBarBackground;
  private static final boolean sEdgeSizeUsingSystemGestureInsets=Build.VERSION.SDK_INT >= 29;
public interface DrawerListener {
    void onDrawerSlide(    View drawerView,    float slideOffset);
    void onDrawerOpened(    View drawerView);
    void onDrawerClosed(    View drawerView);
    void onDrawerStateChanged(    int newState);
  }
  public void setDrawerListener(  DrawerListener listener){
    if (mListener != null) {
      removeDrawerListener(mListener);
    }
    if (listener != null) {
      addDrawerListener(listener);
    }
    mListener=listener;
  }
  public void addDrawerListener(  DrawerListener listener){
    if (mListeners == null) {
      mListeners=new ArrayList<>();
    }
    mListeners.add(listener);
  }
  public void removeDrawerListener(  DrawerListener listener){
    if (mListeners == null) {
      return;
    }
    mListeners.remove(listener);
  }
  public void setDrawerLockMode(  int lockMode){
    setDrawerLockMode(lockMode,Gravity.LEFT);
    setDrawerLockMode(lockMode,Gravity.RIGHT);
  }
  public void setDrawerLockMode(  int lockMode,  int edgeGravity){
    final int absGravity=GravityCompat.getAbsoluteGravity(edgeGravity,ViewCompat.getLayoutDirection(this));
switch (edgeGravity) {
case Gravity.LEFT:
      mLockModeLeft=lockMode;
    break;
case Gravity.RIGHT:
  mLockModeRight=lockMode;
break;
case GravityCompat.START:
mLockModeStart=lockMode;
break;
case GravityCompat.END:
mLockModeEnd=lockMode;
break;
}
if (lockMode != LOCK_MODE_UNLOCKED) {
final ViewDragHelper helper=absGravity == Gravity.LEFT ? mLeftDragger : mRightDragger;
helper.cancel();
}
switch (lockMode) {
case LOCK_MODE_LOCKED_OPEN:
final View toOpen=findDrawerWithGravity(absGravity);
if (toOpen != null) {
openDrawer(toOpen);
}
break;
case LOCK_MODE_LOCKED_CLOSED:
final View toClose=findDrawerWithGravity(absGravity);
if (toClose != null) {
closeDrawer(toClose);
}
break;
}
}
public void setDrawerLockMode(int lockMode,View drawerView){
if (!isDrawerView(drawerView)) {
throw new IllegalArgumentException("View " + drawerView + " is not a "+ "drawer with appropriate layout_gravity");
}
final int gravity=((LayoutParams)drawerView.getLayoutParams()).gravity;
setDrawerLockMode(lockMode,gravity);
}
public int getDrawerLockMode(int edgeGravity){
int layoutDirection=ViewCompat.getLayoutDirection(this);
switch (edgeGravity) {
case Gravity.LEFT:
if (mLockModeLeft != LOCK_MODE_UNDEFINED) {
return mLockModeLeft;
}
int leftLockMode=(layoutDirection == ViewCompat.LAYOUT_DIRECTION_LTR) ? mLockModeStart : mLockModeEnd;
if (leftLockMode != LOCK_MODE_UNDEFINED) {
return leftLockMode;
}
break;
case Gravity.RIGHT:
if (mLockModeRight != LOCK_MODE_UNDEFINED) {
return mLockModeRight;
}
int rightLockMode=(layoutDirection == ViewCompat.LAYOUT_DIRECTION_LTR) ? mLockModeEnd : mLockModeStart;
if (rightLockMode != LOCK_MODE_UNDEFINED) {
return rightLockMode;
}
break;
case GravityCompat.START:
if (mLockModeStart != LOCK_MODE_UNDEFINED) {
return mLockModeStart;
}
int startLockMode=(layoutDirection == ViewCompat.LAYOUT_DIRECTION_LTR) ? mLockModeLeft : mLockModeRight;
if (startLockMode != LOCK_MODE_UNDEFINED) {
return startLockMode;
}
break;
case GravityCompat.END:
if (mLockModeEnd != LOCK_MODE_UNDEFINED) {
return mLockModeEnd;
}
int endLockMode=(layoutDirection == ViewCompat.LAYOUT_DIRECTION_LTR) ? mLockModeRight : mLockModeLeft;
if (endLockMode != LOCK_MODE_UNDEFINED) {
return endLockMode;
}
break;
}
return LOCK_MODE_UNLOCKED;
}
public int getDrawerLockMode(View drawerView){
if (!isDrawerView(drawerView)) {
throw new IllegalArgumentException("View " + drawerView + " is not a drawer");
}
final int drawerGravity=((LayoutParams)drawerView.getLayoutParams()).gravity;
return getDrawerLockMode(drawerGravity);
}
void updateDrawerState(int activeState,View activeDrawer){
final int leftState=mLeftDragger.getViewDragState();
final int rightState=mRightDragger.getViewDragState();
final int state;
if (leftState == STATE_DRAGGING || rightState == STATE_DRAGGING) {
state=STATE_DRAGGING;
}
 else if (leftState == STATE_SETTLING || rightState == STATE_SETTLING) {
state=STATE_SETTLING;
}
 else {
state=STATE_IDLE;
}
if (activeDrawer != null && activeState == STATE_IDLE) {
final LayoutParams lp=(LayoutParams)activeDrawer.getLayoutParams();
if (lp.onScreen == 0) {
dispatchOnDrawerClosed(activeDrawer);
}
 else if (lp.onScreen == 1) {
dispatchOnDrawerOpened(activeDrawer); setFlagStateOpened(activeDrawer);
}
}
if (state != mDrawerState) {
mDrawerState=state;
if (mListeners != null) {
int listenerCount=mListeners.size();
for (int i=listenerCount - 1; i >= 0; i--) {
mListeners.get(i).onDrawerStateChanged(state);
}
}
}
}
void dispatchOnDrawerClosed(View drawerView){
final LayoutParams lp=(LayoutParams)drawerView.getLayoutParams();
if ((lp.openState & LayoutParams.FLAG_IS_OPENED) == 1) {
lp.openState=0;
if (mListeners != null) {
int listenerCount=mListeners.size();
for (int i=listenerCount - 1; i >= 0; i--) {
mListeners.get(i).onDrawerClosed(drawerView);
}
}
updateChildrenImportantForAccessibility(drawerView,false);
updateChildAccessibilityAction(drawerView);
if (hasWindowFocus()) {
final View rootView=getRootView();
if (rootView != null) {
rootView.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
}
}
}
}
void dispatchOnDrawerOpened(View drawerView){
final LayoutParams lp=(LayoutParams)drawerView.getLayoutParams();
if ((lp.openState & LayoutParams.FLAG_IS_OPENED) == 0) {
lp.openState=LayoutParams.FLAG_IS_OPENED;
if (mListeners != null) {
int listenerCount=mListeners.size();
for (int i=listenerCount - 1; i >= 0; i--) {
mListeners.get(i).onDrawerOpened(drawerView);
}
}
updateChildrenImportantForAccessibility(drawerView,true);
updateChildAccessibilityAction(drawerView);
if (hasWindowFocus()) {
sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
}
}
}
void dispatchOnDrawerSlide(View drawerView,float slideOffset){
if (mListeners != null) {
int listenerCount=mListeners.size();
for (int i=listenerCount - 1; i >= 0; i--) {
mListeners.get(i).onDrawerSlide(drawerView,slideOffset);
}
}
}
void setDrawerViewOffset(View drawerView,float slideOffset){
final LayoutParams lp=(LayoutParams)drawerView.getLayoutParams();
if (slideOffset == lp.onScreen) {
return;
}
lp.onScreen=slideOffset;
dispatchOnDrawerSlide(drawerView,slideOffset);
}
float getDrawerViewOffset(View drawerView){
return ((LayoutParams)drawerView.getLayoutParams()).onScreen;
}
int getDrawerViewAbsoluteGravity(View drawerView){
final int gravity=((LayoutParams)drawerView.getLayoutParams()).gravity;
return GravityCompat.getAbsoluteGravity(gravity,ViewCompat.getLayoutDirection(this));
}
boolean checkDrawerViewAbsoluteGravity(View drawerView,int checkFor){
final int absGravity=getDrawerViewAbsoluteGravity(drawerView);
return (absGravity & checkFor) == checkFor;
}
void moveDrawerToOffset(View drawerView,float slideOffset){
final float oldOffset=getDrawerViewOffset(drawerView);
final int width=drawerView.getWidth();
final int oldPos=(int)(width * oldOffset);
final int newPos=(int)(width * slideOffset);
final int dx=newPos - oldPos;
drawerView.offsetLeftAndRight(checkDrawerViewAbsoluteGravity(drawerView,Gravity.LEFT) ? dx : -dx);
setDrawerViewOffset(drawerView,slideOffset);
}
View findDrawerWithGravity(int gravity){
final int absHorizGravity=GravityCompat.getAbsoluteGravity(gravity,ViewCompat.getLayoutDirection(this)) & Gravity.HORIZONTAL_GRAVITY_MASK;
final int childCount=getChildCount();
for (int i=0; i < childCount; i++) {
final View child=getChildAt(i);
final int childAbsGravity=getDrawerViewAbsoluteGravity(child);
if ((childAbsGravity & Gravity.HORIZONTAL_GRAVITY_MASK) == absHorizGravity) {
return child;
}
}
return null;
}
static String gravityToString(int gravity){
if ((gravity & Gravity.LEFT) == Gravity.LEFT) {
return "LEFT";
}
if ((gravity & Gravity.RIGHT) == Gravity.RIGHT) {
return "RIGHT";
}
return Integer.toHexString(gravity);
}
protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec){
int widthMode=MeasureSpec.getMode(widthMeasureSpec);
int heightMode=MeasureSpec.getMode(heightMeasureSpec);
int widthSize=MeasureSpec.getSize(widthMeasureSpec);
int heightSize=MeasureSpec.getSize(heightMeasureSpec);
if (widthMode != MeasureSpec.EXACTLY || heightMode != MeasureSpec.EXACTLY) {
if (isInEditMode()) {
if (widthMode == MeasureSpec.UNSPECIFIED) {
widthSize=300;
}
if (heightMode == MeasureSpec.UNSPECIFIED) {
heightSize=300;
}
}
 else {
throw new IllegalArgumentException("DrawerLayout must be measured with MeasureSpec.EXACTLY.");
}
}
setMeasuredDimension(widthSize,heightSize);
final boolean applyInsets=mLastInsets != null && ViewCompat.getFitsSystemWindows(this);
final int layoutDirection=ViewCompat.getLayoutDirection(this);
boolean hasDrawerOnLeftEdge=false;
boolean hasDrawerOnRightEdge=false;
final int childCount=getChildCount();
for (int i=0; i < childCount; i++) {
final View child=getChildAt(i);
if (child.getVisibility() == GONE) {
continue;
}
final LayoutParams lp=(LayoutParams)child.getLayoutParams();
if (applyInsets) {
final int cgrav=GravityCompat.getAbsoluteGravity(lp.gravity,layoutDirection);
if (ViewCompat.getFitsSystemWindows(child)) {
if (Build.VERSION.SDK_INT >= 21) {
WindowInsetsCompat wi=mLastInsets;
if (cgrav == Gravity.LEFT) {
wi=wi.replaceSystemWindowInsets(wi.getSystemWindowInsetLeft(),wi.getSystemWindowInsetTop(),0,wi.getSystemWindowInsetBottom());
}
 else if (cgrav == Gravity.RIGHT) {
wi=wi.replaceSystemWindowInsets(0,wi.getSystemWindowInsetTop(),wi.getSystemWindowInsetRight(),wi.getSystemWindowInsetBottom());
}
ViewCompat.dispatchApplyWindowInsets(child,wi);
}
}
 else {
if (Build.VERSION.SDK_INT >= 21) {
WindowInsetsCompat wi=mLastInsets;
if (cgrav == Gravity.LEFT) {
wi=wi.replaceSystemWindowInsets(wi.getSystemWindowInsetLeft(),wi.getSystemWindowInsetTop(),0,wi.getSystemWindowInsetBottom());
}
 else if (cgrav == Gravity.RIGHT) {
wi=wi.replaceSystemWindowInsets(0,wi.getSystemWindowInsetTop(),wi.getSystemWindowInsetRight(),wi.getSystemWindowInsetBottom());
}
lp.leftMargin=wi.getSystemWindowInsetLeft();
lp.topMargin=wi.getSystemWindowInsetTop();
lp.rightMargin=wi.getSystemWindowInsetRight();
lp.bottomMargin=wi.getSystemWindowInsetBottom();
}
}
}
if (isContentView(child)) {
final int contentWidthSpec=MeasureSpec.makeMeasureSpec(widthSize - lp.leftMargin - lp.rightMargin,MeasureSpec.EXACTLY);
final int contentHeightSpec=MeasureSpec.makeMeasureSpec(heightSize - lp.topMargin - lp.bottomMargin,MeasureSpec.EXACTLY);
child.measure(contentWidthSpec,contentHeightSpec);
}
 else if (isDrawerView(child)) {
if (SET_DRAWER_SHADOW_FROM_ELEVATION) {
if (ViewCompat.getElevation(child) != mDrawerElevation) {
ViewCompat.setElevation(child,mDrawerElevation);
}
}
final int childGravity=getDrawerViewAbsoluteGravity(child) & Gravity.HORIZONTAL_GRAVITY_MASK;
boolean isLeftEdgeDrawer=(childGravity == Gravity.LEFT);
if ((isLeftEdgeDrawer && hasDrawerOnLeftEdge) || (!isLeftEdgeDrawer && hasDrawerOnRightEdge)) {
throw new IllegalStateException("Child drawer has absolute gravity " + gravityToString(childGravity) + " but this "+ TAG+ " already has a "+ "drawer view along that edge");
}
if (isLeftEdgeDrawer) {
hasDrawerOnLeftEdge=true;
}
 else {
hasDrawerOnRightEdge=true;
}
final int drawerWidthSpec=getChildMeasureSpec(widthMeasureSpec,mMinDrawerMargin + lp.leftMargin + lp.rightMargin,lp.width);
final int drawerHeightSpec=getChildMeasureSpec(heightMeasureSpec,lp.topMargin + lp.bottomMargin,lp.height);
child.measure(drawerWidthSpec,drawerHeightSpec);
}
 else {
throw new IllegalStateException("Child " + child + " at index "+ i+ " does not have a valid layout_gravity - must be Gravity.LEFT, "+ "Gravity.RIGHT or Gravity.NO_GRAVITY");
}
}
}
protected void onLayout(boolean changed,int l,int t,int r,int b){
mInLayout=true;
final int width=r - l;
final int childCount=getChildCount();
for (int i=0; i < childCount; i++) {
final View child=getChildAt(i);
if (child.getVisibility() == GONE) {
continue;
}
final LayoutParams lp=(LayoutParams)child.getLayoutParams();
if (isContentView(child)) {
child.layout(lp.leftMargin,lp.topMargin,lp.leftMargin + child.getMeasuredWidth(),lp.topMargin + child.getMeasuredHeight());
}
 else {
final int childWidth=child.getMeasuredWidth();
final int childHeight=child.getMeasuredHeight();
int childLeft;
final float newOffset;
if (checkDrawerViewAbsoluteGravity(child,Gravity.LEFT)) {
childLeft=-childWidth + (int)(childWidth * lp.onScreen);
newOffset=(float)(childWidth + childLeft) / childWidth;
}
 else {
childLeft=width - (int)(childWidth * lp.onScreen);
newOffset=(float)(width - childLeft) / childWidth;
}
final boolean changeOffset=newOffset != lp.onScreen;
final int vgrav=lp.gravity & Gravity.VERTICAL_GRAVITY_MASK;
switch (vgrav) {
default :
case Gravity.TOP:
{
child.layout(childLeft,lp.topMargin,childLeft + childWidth,lp.topMargin + childHeight);
break;
}
case Gravity.BOTTOM:
{
final int height=b - t;
child.layout(childLeft,height - lp.bottomMargin - child.getMeasuredHeight(),childLeft + childWidth,height - lp.bottomMargin);
break;
}
case Gravity.CENTER_VERTICAL:
{
final int height=b - t;
int childTop=(height - childHeight) / 2;
if (childTop < lp.topMargin) {
childTop=lp.topMargin;
}
 else if (childTop + childHeight > height - lp.bottomMargin) {
childTop=height - lp.bottomMargin - childHeight;
}
child.layout(childLeft,childTop,childLeft + childWidth,childTop + childHeight);
break;
}
}
if (changeOffset) {
setDrawerViewOffset(child,newOffset);
}
final int newVisibility=lp.onScreen > 0 ? VISIBLE : INVISIBLE;
if (child.getVisibility() != newVisibility) {
child.setVisibility(newVisibility);
}
}
}
if (sEdgeSizeUsingSystemGestureInsets) {
WindowInsetsCompat rootInsets=(WindowInsetsCompat)ViewCompat.getRootWindowInsets(this);
if (rootInsets != null) {
r.android.graphics.Insets gestureInsets=rootInsets.getSystemGestureInsets();
mLeftDragger.setEdgeSize(Math.max(mLeftDragger.getDefaultEdgeSize(),gestureInsets.left));
mRightDragger.setEdgeSize(Math.max(mRightDragger.getDefaultEdgeSize(),gestureInsets.right));
}
}
mInLayout=false;
mFirstLayout=false;
}
boolean isContentView(View child){
return ((LayoutParams)child.getLayoutParams()).gravity == Gravity.NO_GRAVITY;
}
boolean isDrawerView(View child){
final int gravity=((LayoutParams)child.getLayoutParams()).gravity;
final int absGravity=GravityCompat.getAbsoluteGravity(gravity,ViewCompat.getLayoutDirection(child));
if ((absGravity & Gravity.LEFT) != 0) {
return true;
}
return (absGravity & Gravity.RIGHT) != 0;
}
public void openDrawer(View drawerView){
openDrawer(drawerView,true);
}
public void openDrawer(View drawerView,boolean animate){
if (!isDrawerView(drawerView)) {
throw new IllegalArgumentException("View " + drawerView + " is not a sliding drawer");
}
final LayoutParams lp=(LayoutParams)drawerView.getLayoutParams();
if (mFirstLayout) {
lp.onScreen=1.f;
lp.openState=LayoutParams.FLAG_IS_OPENED;
updateChildrenImportantForAccessibility(drawerView,true);
updateChildAccessibilityAction(drawerView);
}
 else if (animate) {
lp.openState|=LayoutParams.FLAG_IS_OPENING;
if (checkDrawerViewAbsoluteGravity(drawerView,Gravity.LEFT)) {
mLeftDragger.smoothSlideViewTo(drawerView,0,drawerView.getTop());
}
 else {
mRightDragger.smoothSlideViewTo(drawerView,getWidth() - drawerView.getWidth(),drawerView.getTop());
}
}
 else {
moveDrawerToOffset(drawerView,1.f);
updateDrawerState(STATE_IDLE,drawerView);
drawerView.setVisibility(VISIBLE);
}
invalidate();
}
public void openDrawer(int gravity){
openDrawer(gravity,true);
}
public void openDrawer(int gravity,boolean animate){
final View drawerView=findDrawerWithGravity(gravity);
if (drawerView == null) {
throw new IllegalArgumentException("No drawer view found with gravity " + gravityToString(gravity));
}
openDrawer(drawerView,animate);
}
public void closeDrawer(View drawerView){
closeDrawer(drawerView,true);
}
public void closeDrawer(View drawerView,boolean animate){
if (!isDrawerView(drawerView)) {
throw new IllegalArgumentException("View " + drawerView + " is not a sliding drawer");
}
final LayoutParams lp=(LayoutParams)drawerView.getLayoutParams();
if (mFirstLayout) {
lp.onScreen=0.f;
lp.openState=0;
}
 else if (animate) {
lp.openState|=LayoutParams.FLAG_IS_CLOSING;
if (checkDrawerViewAbsoluteGravity(drawerView,Gravity.LEFT)) {
mLeftDragger.smoothSlideViewTo(drawerView,-drawerView.getWidth(),drawerView.getTop());
}
 else {
mRightDragger.smoothSlideViewTo(drawerView,getWidth(),drawerView.getTop());
}
}
 else {
moveDrawerToOffset(drawerView,0.f);
updateDrawerState(STATE_IDLE,drawerView);
drawerView.setVisibility(INVISIBLE);
}
invalidate();
}
public void closeDrawer(int gravity){
closeDrawer(gravity,true);
}
public void closeDrawer(int gravity,boolean animate){
final View drawerView=findDrawerWithGravity(gravity);
if (drawerView == null) {
throw new IllegalArgumentException("No drawer view found with gravity " + gravityToString(gravity));
}
closeDrawer(drawerView,animate);
}
public boolean isDrawerOpen(View drawer){
if (!isDrawerView(drawer)) {
throw new IllegalArgumentException("View " + drawer + " is not a drawer");
}
LayoutParams drawerLp=(LayoutParams)drawer.getLayoutParams();
return (drawerLp.openState & LayoutParams.FLAG_IS_OPENED) == 1;
}
public boolean isDrawerOpen(int drawerGravity){
final View drawerView=findDrawerWithGravity(drawerGravity);
if (drawerView != null) {
return isDrawerOpen(drawerView);
}
return false;
}
public static class LayoutParams extends ViewGroup.MarginLayoutParams {
private static final int FLAG_IS_OPENED=0x1;
private static final int FLAG_IS_OPENING=0x2;
private static final int FLAG_IS_CLOSING=0x4;
public int gravity=Gravity.NO_GRAVITY;
float onScreen;
boolean isPeeking;
int openState;
public LayoutParams(int width,int height){
super(width,height);
}
public LayoutParams(int width,int height,int gravity){
this(width,height);
this.gravity=gravity;
}
public LayoutParams(LayoutParams source){
super(source);
this.gravity=source.gravity;
}
public LayoutParams(ViewGroup.LayoutParams source){
super(source);
}
}
class ViewDragHelper {
public static final int STATE_IDLE=0;
public static final int STATE_DRAGGING=1;
public static final int STATE_SETTLING=2;
private static final int EDGE_SIZE=20;
private int mDragState;
private int mEdgeSize;
private final int mDefaultEdgeSize;
public int getViewDragState(){
return mDragState;
}
public void cancel(){
}
public ViewDragHelper(r.android.content.Context context){
final float density=context.getResources().getDisplayMetrics().density;
mDefaultEdgeSize=(int)(EDGE_SIZE * density + 0.5f);
}
public int getDefaultEdgeSize(){
return mDefaultEdgeSize;
}
public void setEdgeSize(int size){
mEdgeSize=size;
}
public void smoothSlideViewTo(View drawerView,int x,int y){
DrawerLayout.this.smoothSlideViewTo(drawerView,x,y);
}
}
class WindowInsetsCompat {
public int getSystemWindowInsetLeft(){
return 0;
}
public WindowInsetsCompat replaceSystemWindowInsets(int systemWindowInsetLeft,int systemWindowInsetTop,int systemWindowInsetRight,int systemWindowInsetBottom){
return null;
}
public int getSystemWindowInsetBottom(){
return 0;
}
public int getSystemWindowInsetRight(){
return 0;
}
public int getSystemWindowInsetTop(){
return 0;
}
public r.android.graphics.Insets getSystemGestureInsets(){
return null;
}
}
public DrawerLayout(){
final float density=getResources().getDisplayMetrics().density;
mMinDrawerMargin=(int)(MIN_DRAWER_MARGIN * density + 0.5f);
mLeftDragger=new ViewDragHelper(getContext());
mRightDragger=new ViewDragHelper(getContext());
}
private void updateChildAccessibilityAction(View drawerView){
}
private void updateChildrenImportantForAccessibility(View drawerView,boolean b){
}
public void smoothSlideViewTo(View drawerView,int x,int y){
}
public void updateDrawerViewState(int activeState,View activeDrawer){
updateDrawerState(activeState,activeDrawer);
}
public void moveDrawerViewToOffset(View drawerView,float slideOffset){
moveDrawerToOffset(drawerView,slideOffset);
}
public boolean isViewContentView(View view){
return isContentView(view);
}
public boolean requiresToBeOnScreen(View view){
return (((DrawerLayout.LayoutParams)view.getLayoutParams())).onScreen == 1;
}
public boolean isOpening(View drawerView){
LayoutParams layoutParams=((DrawerLayout.LayoutParams)drawerView.getLayoutParams());
int openState=layoutParams.openState;
return (openState & LayoutParams.FLAG_IS_OPENING) != 0;
}
public void onViewPositionChanged(View changedView,int left,int top,int dx,int dy){
float offset;
final int childWidth=changedView.getWidth();
if (checkDrawerViewAbsoluteGravity(changedView,Gravity.LEFT)) {
offset=(float)(childWidth + left) / childWidth;
}
 else {
final int width=getWidth();
offset=(float)(width - left) / childWidth;
}
setDrawerViewOffset(changedView,offset);
}
void setFlagStateOpened(View drawerView){
final LayoutParams lp=(LayoutParams)drawerView.getLayoutParams();
lp.openState=LayoutParams.FLAG_IS_OPENED;
}
}
