var obj = {
    print: function(){
        var a1 = "b";
        console.log(a1);
    },
    print2: function(){
        var a2 = "c";
        var inner = function (){
            console.log(this);
        };

        return inner;
    }
};
var a = obj.print2();
a();
