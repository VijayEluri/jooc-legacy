Class: abstract class {
	
	/// Number of bytes to allocate for a new instance of this class 
	size: SizeT

	/// Human readable representation of the name of this class
    name: String
	
	/// Pointer to instance of super-class
	super: const Class
	
	/// Create a new instance of the object of type defined by this class
	alloc: final func -> Object {
		object := gc_malloc(size) as Object
		if(object) {
			object class = this
			object defaults()
		}
		return object
	}
	
	// workaround needed to avoid C circular dependency with _ObjectClass
	defaults: static Func (Class)
	destroy: static Func (Class)
	
}

Object: abstract class {

	class: Class
	
	/// Instance initializer: set default values for a new instance of this class
	defaults: func
	
	/// Finalizer: cleans up any objects belonging to this instance
	destroy: func
	
}