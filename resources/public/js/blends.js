function update_requested_blends() {
    $("#requested-blends").empty();
    $.getJSON( "/requested-blends.json", function( data ) {
        var items = [];
        $.each( data, function( index, item ) {
            items.push( "<li class='trigger' id='" + item.wave_id2 + "'>" + 
                        "<span>" + item.name + "</span>" +
                        "<ul class='menu'>" +
                        "<li>" +
                        "<img src=\"/i/accept.png\" height=\"15\">" + 
                        "<img src=\"/i/reject.png\" height=\"15\">" + 
                        "</li>" +
                        "</ul>" +
                        "</li>" );
        });
        $("#requested-blends").append(items);
    });
}

function update_blended_with() {
    $("#blended-with").empty();
    $.getJSON( "/blended-with.json", function( data ) {
        var items = [];
        $.each( data, function( index, item ) {
            items.push( "<li class='trigger' id='" + item.id + "'>" +
                        "<span>" + item.name + "</span>" +
                        "<ul class='menu'>" +
                        "<li>" +
                        "<img src=\"/i/unblend.png\" height=\"15\">" +
                        "</li>" +
                        "</ul>" +
                        "</li>" );
        });
        $("#blended-with").append(items);
    });
}

function update_unconfirmed_blends() {
    $("#unconfirmed-blends").empty();
    $.getJSON( "/unconfirmed-blends.json", function( data ) {
        var items = [];
        $.each( data, function( index, item ) {
            items.push( "<li class='trigger' id='" + item.wave_id2 + "'>" + 
                        "<span>" + item.name + "</span>" +
                        "<ul class='menu'>" +
                        "<li>" +
                        "<img src=\"/i/unblend.png\" height=\"15\">" + 
                        "</li>" +
                        "</ul>" +
                        "</li>" );
        });
        $("#unconfirmed-blends").append(items);
    });
}

function update_all_blends() {
    update_requested_blends();
    update_blended_with();
    update_unconfirmed_blends();
    $("#wave_name").val("");
}


$(function() {
    $( "#wave_name" ).autocomplete({
        source: "/autocomplete-wave-name.json",
        minLength: 2,
        select: function( event, ui ) {
            $.ajax({
                type: "POST",
                url: "/request-blending.json",
                data: JSON.stringify({wave_name: ui.item.label}),
                contentType: "application/json; charset=utf-8",
                dataType: "json"
                ,
                success: function(data){
                    // alert(data);
                    update_all_blends();
                },
                failure: function(errMsg) {
                    // alert(errMsg);
                    update_all_blends();
                }
            });


        }
    });
});

update_all_blends();
