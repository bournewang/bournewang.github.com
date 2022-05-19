---
layout: post
title:  "Scala Programming - Part 2"
date:   2022-05-19 21:59:15 +0800
categories: language
tags:
    - Bigdata
    - Scala
---

## Formatting
```scala
scala> "Application %s".format("Hello world")
val res118: String = Application Hello world

scala> "age %d".format(5)
val res119: String = age 5

scala> "char %c".format('a')
val res120: String = char a

scala> "name %s, age %d".format("Marry", 20)
val res122: String = name Marry, age 20
```
## Class and Object 
```scala
class Point (val xc:Int, val yc: Int){
  var x: Int = xc
  var y: Int = yc

  def move(dx:Int, dy:Int): Unit = {
    x += dx
    y += dy
    println("new x: "+x)
    println("new y: "+y)
  }

}

class Location(override val xc:Int, override val yc:Int, val zc:Int)
  extends Point(xc, yc){
  var z:Int = zc
  def move(dx:Int, dy:Int, dz:Int): Unit = {
    x += dx
    y += dy
    z += dz
    println("new x: "+x)
    println("new y: "+y)
    println("new z: "+z)
  }
}
object Point{
  def main(args: Array[String]): Unit = {
    val p = new Point(10,10)
    p.move(2,3)
    //    new x: 12
    //    new y: 13

    val l = new Location(10, 10, 10)
    l.move(3,4,5)
    //    new x: 13
    //    new y: 14
    //    new z: 15
  }
}

```

## Trait
like interface class in Java:
```scala
trait Animal {
  def love():Unit
}

class Dog extends Animal{
  def love():Unit = println("dog love meat")
}

object Animal{
  def main(args: Array[String]): Unit = {
    var dog = new Dog
    dog.love
    //dog love meat
  }
}
```

## Pattern Match
```scala
object Animal{
  def main(args: Array[String]): Unit = {
    println(match1(1))
    //    one
    println(match1(2))
    //    two
    println(match1(10))
    //    many
  }

  def match1(x:Int):String = x match{
    case 1 => "one"
    case 2 => "two"
    case _ => "many"
  }
}

```

## Case Class
```scala
case class Person(name:String, age:Int)

object Person{
  def main(args: Array[String]): Unit = {
    val marry = new Person("Marry", 20)
    val charles = new Person("Charles", 18)
    val tomas = new Person("Tomas", 33)

    for (person <- List(marry, charles, tomas)) {
      person match {
        case Person("Marry", 20) => println("Marry is 20")
        case Person("Charles", 18) => println("Charles is 18")
        case Person(name, age) => println("name: "+name+", age: "+age)
      }
    }

//    Marry is 20
//    Charles is 18
//    name: Tomas, age: 33
  }
}
```


## Regular expression
```scala
scala> val p = "h[a-z]+".r
val p: scala.util.matching.Regex = h[a-z]+

scala> p.findFirstIn("hello, how are you")
val res91: Option[String] = Some(hello)

scala> val res = p.findAllIn("hello, how are you")
val res: scala.util.matching.Regex.MatchIterator = <iterator>

scala> res.next()
val res99: String = hello

scala> res.next()
val res100: String = how
```


## Exception
```scala
import java.io.{FileNotFoundException, FileReader}

object Example3 {
  def main(args: Array[String]): Unit = {
    try {
      val f = new FileReader("file1.txt")
      //    file not found
      val a = 2/0
      //    other exception
    }catch{
      case ex: FileNotFoundException => println("file not found")
      case ex: Exception => println("other exception")
    }
  }
}

```


## Extractor
Extract data from an instance, when you assign an instance to and class/object, it will trigger the unapply method to extract data.
```scala
object Extractor {
  def main(args: Array[String]): Unit = {
    val obj = Extractor("Marry")
    val Extractor(name) = obj
    println(name)
    // Marry
  }

  def apply(name: String) = s"hello-$name"
  def unapply(str: String):Option[String] = {
    var arr = str.split("-")
    if (arr.nonEmpty) {
      Some(arr(1))
    }else{
      None
    }
  }
}

```

## File IO
```scala
import scala.io.StdIn

object IO {
  def main(args: Array[String]): Unit = {
    val line = StdIn.readLine()
    println("your input was: "+line)
    
    // hello, how are you
    // your input was: hello, how are you
    
    Source.fromFile("test.txt").foreach(print)
  }
}
```


## Generic
```scala
package Day05

class Generics[A] {
  private var m:A = _
  def set(x:A):Unit = m=x
  def get():A = m
}

object Generics {
  def main(args: Array[String]): Unit = {
    val g = new Generics[String];
    g.set("Hello")
    println(g.get)
    // Hello

    val g1 = new Generics[Int]
    g1.set(100)
    println(g1.get)
    // 100
  }
}

```
