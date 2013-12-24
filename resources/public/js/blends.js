$.getJSON( "/requested-blends.json", function( data ) {
    var items = [];
    $.each( data, function( index, item ) {
        items.push( "<li class='requested-blend' id='" + item.wave_id2 + "'>" + item.name + "</li>" );
    });
    $("#requested-blends").append(items);
});


$.getJSON( "/blended-with.json", function( data ) {
    var items = [];
    $.each( data, function( index, item ) {
        items.push( "<li id='" + item.id + "'>" + item.name + "</li>" );
    });
    $("#blended-with").append(items);
});

$.getJSON( "/unconfirmed-blends.json", function( data ) {
    var items = [];
    $.each( data, function( index, item ) {
        items.push( "<li class='unconfirmed-blend' id='" + item.wave_id2 + "'>" + item.name + "</li>" );
    });
    $("#unconfirmed-blends").append(items);
});



$(function() {
    function complete_selection( message ) {
        $( "<div>" ).text( message ).prependTo( "#requested-blends" );
    }
    $( "#wave_name" ).autocomplete({
        source: "/autocomplete-wave-name.json",
        minLength: 2,
        select: function( event, ui ) {
            alert(ui.item.value);
            $.ajax({
                type: "POST",
                url: "/request-blending.json",
                // The key needs to match your method's input parameter (case-sensitive).
                data: JSON.stringify({wave_name: ui.item.label}),
                contentType: "application/json; charset=utf-8",
                dataType: "json",
                success: function(data){alert(data);},
                failure: function(errMsg) {
                    alert(errMsg);
                }
            });

            complete_selection( ui.item ?
                 "Selected: " + ui.item.label :
                 "Nothing selected, input was " + this.value );


        }
    });
});


