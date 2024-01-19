let login_form = jQuery("#login_form");
console.log("login is attempting to run")
function handleLoginResult(resultDataJson) {
    console.log(resultDataJson);
    /*let resultDataJson = JSON.parse(resultDataString);*/

    console.log("handle login response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    if (resultDataJson["status"] === "success") {
        window.location.replace("index.html");
    } else {
        console.log("show error message");
        console.log(resultDataJson["message"]);
        jQuery("#login_error_message").text(resultDataJson["message"]).css("color", "red");
    }
}

function submitLoginForm(formSubmitEvent) {
    console.log("submit login form");
    formSubmitEvent.preventDefault();
    jQuery.ajax("api/login",{
            method: "POST",
            data: login_form.serialize(),
            success: handleLoginResult
        }
    );
}

login_form.submit(submitLoginForm);