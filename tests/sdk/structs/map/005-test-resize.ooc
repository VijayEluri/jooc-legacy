import structs/HashMap

main: func {
 
    map := HashMap<String> new(3)
    
    for(i in 0..10) {
        s := i toString()
        map put(s, s)
    }
    
}
