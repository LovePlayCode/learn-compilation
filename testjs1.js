var obj = {
    print: function(){
        var a1 = "b";
        console.log(a1);
    },
    print2: function(){
        var a2 = "c";
        function inner3(){
            console.log(this);
        }

        return inner3;
    }
};
var a = obj.print2();
a();
