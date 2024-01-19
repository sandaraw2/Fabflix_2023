function addToCart(movieID){
    console.log("Adding Movie to Cart");

    jQuery.ajax({
        method: "POST",
        data:{decrease: false,
            deleteID: false},
        url: "api/addToCart?id=" + movieID,
        error: function(xhr, status, error) {
            console.error('Error:', error);}
    });

    window.alert("Added to Cart!");
}
