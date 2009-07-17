/*
 * Generated by ooc, the Object-Oriented C compiler, by Amos Wenger, 2009
 */

// OOC dependencies
#include "eventmanagertest.h"
/**
 * Test suite for ooc's event manager.
 * Output and expected output must be the same.
 * Copyright (C) 2009 Adrien Béraud <adrienberaud@gmail.com>
 * Published under the General Public Licence 3.0
 */


/**
 * Simple class that throw events.
 */

/*
 * Definition of class TestObject
 */

TestObject__class TestObject__classInstance;


Void __TestObject_clic(struct TestObject*  this) {
	
	this->class->__dispatchEvent_Event(this, __MouseEvent_new_Int_Int_Int(MouseEvent_CLIC, 36, 30));


}

Void __TestObject_over(struct TestObject*  this) {
	
	this->class->__dispatchEvent_Event(this, __MouseEvent_new_Int_Int_Int(MouseEvent_OVER, 50, 30));


}

struct TestObject*  __TestObject_new_String(String obj_name) {

	TestObject this = GC_malloc(sizeof(struct TestObject));

	if(TestObject__classInstance == NULL) {
		TestObject__classInstance = GC_malloc(sizeof(struct TestObject__class));
		TestObject__classInstance->name = "TestObject";
		TestObject__classInstance->simpleName = "TestObject";
	}
	this->class = TestObject__classInstance;

	this->class->__addEventListener_Func_Int = (Void (*)(struct TestObject* , Func, Int)) &__event_EventDispatcher_addEventListener_Func_Int;
	this->class->__removeEventListener_Func_Int = (Bool (*)(struct TestObject* , Func, Int)) &__event_EventDispatcher_removeEventListener_Func_Int;
	this->class->__dispatchEvent_Event = (Void (*)(struct TestObject* , struct event_Event* )) &__event_EventDispatcher_dispatchEvent_Event;
	this->class->__clic = (Void (*)(struct TestObject* )) &__TestObject_clic;
	this->class->__over = (Void (*)(struct TestObject* )) &__TestObject_over;
	
	this->obj_name = obj_name;
	this->listeners = __structs_SparseList_new_Int(1);;

	return this;


}

Int main() {

	GC_init();	
	puts("Expected output :");
	puts("Event received from object1 : clic");
	puts("Event received from object2 : over");
	puts("Event received from object1 : over");
	puts("Event received from object2 : clic");
	puts("Output :");
	struct TestObject*  obj1 = __TestObject_new_String("object1");
	struct TestObject*  obj2 = __TestObject_new_String("object2");
	obj1->class->__addEventListener_Func_Int(obj1, __cool_MouseEvent, MouseEvent_CLIC);
	obj2->class->__addEventListener_Func_Int(obj2, __cool_MouseEvent, MouseEvent_OVER);
	obj1->class->__clic(obj1);
	obj1->class->__over(obj1);
	obj2->class->__clic(obj2);
	obj2->class->__over(obj2);
	obj1->class->__removeEventListener_Func_Int(obj1, __cool_MouseEvent, MouseEvent_CLIC);
	obj2->class->__removeEventListener_Func_Int(obj2, __cool_MouseEvent, MouseEvent_OVER);
	obj1->class->__clic(obj1);
	obj1->class->__over(obj1);
	obj2->class->__clic(obj2);
	obj2->class->__over(obj2);
	obj1->class->__addEventListener_Func_Int(obj1, __cool_MouseEvent, MouseEvent_OVER);
	obj2->class->__addEventListener_Func_Int(obj2, __cool_MouseEvent, MouseEvent_CLIC);
	obj1->class->__addEventListener_Func_Int(obj1, __cool_MouseEvent, MouseEvent_OVER);
	obj2->class->__addEventListener_Func_Int(obj2, __cool_MouseEvent, MouseEvent_CLIC);
	obj1->class->__addEventListener_Func_Int(obj1, __cool_MouseEvent, MouseEvent_OVER);
	obj2->class->__addEventListener_Func_Int(obj2, __cool_MouseEvent, MouseEvent_CLIC);
	obj1->class->__clic(obj1);
	obj1->class->__over(obj1);
	obj2->class->__clic(obj2);
	obj2->class->__over(obj2);


}

Void __cool_MouseEvent(struct MouseEvent*  e) {
	
	struct TestObject*  target = e->target;
	String obj_name = target->obj_name;
	printf("Event received from %s : ", obj_name);
	switch(e->type) {
		case MouseEvent_CLIC :  printf("clic\n");
		
		break;
		case MouseEvent_OVER :  printf("over\n");
		
		break;
	}

	/*
	 * if(e.type == MouseEvent.CLIC) printf("clic\n");
	 * else if(e.type == MouseEvent.OVER) printf("over\n");
	 */



}
