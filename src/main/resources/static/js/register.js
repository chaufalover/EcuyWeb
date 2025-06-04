function register(dato){
    let reg_email = '/^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$/g';
    if(dato.match(reg_email)){
        console.log("correcto")
    }else {
        console.log("Incorrecto")
    }
    
}
console.log(register("email.com"))
    console.log(register("email@gmail.com"))