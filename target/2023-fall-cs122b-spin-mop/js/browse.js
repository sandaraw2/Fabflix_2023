function browseByLetter(letter){
    console.log("browsing by letter")
    // Make the HTTP GET request and register the success callback function
    jQuery.ajax({
        dataType: "json", // Setting return data type
        data:{letter: letter},
        method: "GET", // Setting request method
        url: "api/browse", // Setting request URL
        success: () => displayBrowseResults() // Setting callback function
    });
}



function browseByGenre(genre){
    console.log("browsing by genre")
    // Make the HTTP GET request and register the success callback function
    jQuery.ajax({
        dataType: "json", // Setting return data type
        data:{genre: genre},
        method: "GET", // Setting request method
        url: "api/browse", // Setting request URL
        success: () => displayBrowseResults() // Setting callback function
    });
}


function generateGenreButtons(resultData){
    let genreButtonsElement = jQuery("#genre_buttons_container");
    //For each genre from resultData create button

    for (let i = 0; i < resultData.length; i++) {
        let encodedGenre = encodeURIComponent(resultData[i]);

        let buttonElement = jQuery("<button class=\"btn btn-slay\">")
            .text(resultData[i])
            .on("click", function() {
                browseByGenre(encodedGenre);});

        genreButtonsElement.append(buttonElement);
    }

}

function generateLetterButtons(){
    let letterButtonsElement = jQuery("#letter_buttons");
    const letters = ["A", "B", "C", "D", "E", "F", "G", "H", "I",
                                "J", "K", "L", "N", "O", "P", "Q", "R", "S",
                                "T", "U", "V", "W", "X", "Y", "Z", "*"];
    //For each genre from resultData create button

    for (let i = 0; i < letters.length; i++) {

        let buttonElement = jQuery("<button class=\"btn btn-slay\">")
            .text(letters[i])
            .on("click", function() {
                browseByLetter(letters[i]);});

        letterButtonsElement.append(buttonElement);
    }

}



function displayBrowseResults() {
    // Redirect to the movie link
    //window.location.href = "movielist.html";
    window.location.replace("movielist.html");

}

jQuery(document).ready(function() {
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: "api/genres", // Setting request url, which is mapped by StarsServlet in Star.java
        cache:false,
        success: function (resultData) {
            generateGenreButtons(resultData)
            generateLetterButtons();
        },
        error: function(xhr, status, error) {
            console.error("Error:", status, error);}
    });
});