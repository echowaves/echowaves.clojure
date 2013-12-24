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



$( "#wave_name" ).autocomplete({
    source: function( request, response ) {
        $.ajax({
            url: "/autocomplete-wave-name.json",
            dataType: "json",
            data: {
                featureClass: "P",
                style: "full",
                maxRows: 12,
                name_startsWith: request.term
            },
            success: function( data ) {
                response( $.map( data.geonames, function( item ) {
                    return {
                        label: item.name + (item.adminName1 ? ", " + item.adminName1 : "") + ", " + item.countryName,
                        value: item.name
                    }
                }));
            }
        });
    },
    minLength: 2,
    select: function( event, ui ) {
        log( ui.item ?
             "Selected: " + ui.item.label :
             "Nothing selected, input was " + this.value);
    },
    open: function() {
        $( this ).removeClass( "ui-corner-all" ).addClass( "ui-corner-top" );
    },
    close: function() {
        $( this ).removeClass( "ui-corner-top" ).addClass( "ui-corner-all" );
    }
});

