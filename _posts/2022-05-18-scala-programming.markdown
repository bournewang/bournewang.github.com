---
layout: post
title:  "Scala Programming - Part 1"
date:   2022-05-18 21:59:15 +0800
categories: language
tags:
    - Bigdata
    - Scala
---

Array/List/Set have some common features and functions, such as: 
* _size_
* _min_
* _max_
* _sum_
* _head_
* _tail_
* _concat_
* _forall_
* _foreach_
* _map_
* _filter_
* _find_
* _mkString_

# 1. Collection
## 1.1 Array
```scala
scala> var a1 = List(1,2,3)
var a1: List[Int] = List(1, 2, 3)

scala> a1(0)
val res144: Int = 1

scala> a1(1)
val res145: Int = 2

scala> var a2 = Array("Hello", "World")
var a2: Array[String] = Array(Hello, World)

scala> a2(0)
val res146: String = Hello

scala> for (x <- a1)
     | println("element of a1: "+x)
element of a1: 1
element of a1: 2
element of a1: 3

scala> for (y <- a2)
     | println("element of a1: "+y)
element of a1: Hello
element of a1: World

scala> a1.max
val res149: Int = 3

scala> a2.max
val res150: String = World

scala> var m = Array.ofDim[Int](3,3)
var m: Array[Array[Int]] = Array(Array(0, 0, 0), Array(0, 0, 0), Array(0, 0, 0))

scala> m(0)(0) = 0

scala> m(0)(1) = 1

scala> m
val res153: Array[Array[Int]] = Array(Array(0, 1, 0), Array(0, 0, 0), Array(0, 0, 0))

scala> a1.concat(a2)
val res154: List[Any] = List(1, 2, 3, Hello, World)

scala> a1
val res155: List[Int] = List(1, 2, 3)

scala> a1 concat a2
val res156: List[Any] = List(1, 2, 3, Hello, World)

scala> Array.empty
val res161: Array[Nothing] = Array()

scala> Array.fill(10)(1)
val res158: Array[Int] = Array(1, 1, 1, 1, 1, 1, 1, 1, 1, 1)

scala> Array.fill(3,3)(1)
val res164: Array[Array[Int]] = Array(Array(1, 1, 1), Array(1, 1, 1), Array(1, 1, 1))

scala> Array.range(1,10)
val res166: Array[Int] = Array(1, 2, 3, 4, 5, 6, 7, 8, 9)

scala> Array.range(1,10,2)
val res167: Array[Int] = Array(1, 3, 5, 7, 9)

scala> a1.map((x:Int) => println(x))
1
2
3
val res168: List[Unit] = List((), (), ())

scala> a2.map((x:String) => println(x))
Hello
World
val res170: Array[Unit] = Array((), ())
```

## 1.2 List
```scala
scala> val langs = List("Python", "Java", "Scala")
val langs: List[String] = List(Python, Java, Scala)

scala> val nums = List(1,2,3,4)
val nums: List[Int] = List(1, 2, 3, 4)

scala> val dim: List[List[Int]] = List(List(1,0,0), List(2,0,0), List(3,0,0))
val dim: List[List[Int]] = List(List(1, 0, 0), List(2, 0, 0), List(3, 0, 0))

scala> langs.head
val res171: String = Python

scala> langs.tail
val res172: List[String] = List(Java, Scala)

scala> Nil.isEmpty
val res173: Boolean = true

scala> Nil
val res174: collection.immutable.Nil.type = List()

scala> nums.isEmpty
val res175: Boolean = false

scala> langs.concat(nums)
val res176: List[Any] = List(Python, Java, Scala, 1, 2, 3, 4)

scala> nums.max
val res177: Int = 4

scala> nums.min
val res178: Int = 1

scala> List.fill(10)(2)
val res180: List[Int] = List(2, 2, 2, 2, 2, 2, 2, 2, 2, 2)

scala> List.fill(3,3)(2)
val res181: List[List[Int]] = List(List(2, 2, 2), List(2, 2, 2), List(2, 2, 2))

scala> List.fill(3,3)("Hello")
val res182: List[List[String]] = List(List(Hello, Hello, Hello), List(Hello, Hello, Hello), List(Hello, Hello, Hello))

scala> List.tabulate(6)(n => n*n)
val res183: List[Int] = List(0, 1, 4, 9, 16, 25)

scala> nums.reverse
val res184: List[Int] = List(4, 3, 2, 1)

scala> nums(0)
val res185: Int = 1

scala> nums(1)
val res186: Int = 2

scala> langs(1)
val res187: String = Java

scala> val nums1 = nums :+ 2
val nums1: List[Int] = List(1, 2, 3, 4, 2)

scala> nums :+ 5
val res188: List[Int] = List(1, 2, 3, 4, 5)

scala> langs :+ "Go"
val res189: List[String] = List(Python, Java, Scala, Go)

scala> langs.contains("ava")
val res192: Boolean = false

scala> langs.contains("Java")
val res193: Boolean = true

scala> nums.filter((x:Int) => x>2)
val res194: List[Int] = List(3, 4)

scala> langs.forall(s=>s.startsWith("J"))
val res196: Boolean = false

scala> langs.foreach((x:String) => println(x))
Python
Java
Scala

scala> langs.map((x:String) => println(x))
Python
Java
Scala
val res198: List[Unit] = List((), (), ())

scala> nums.last
val res199: Int = 4

scala> langs.last
val res200: String = Scala

scala> langs.lastIndexOf("Go")
val res202: Int = -1

scala> langs.lastIndexOf("Python")
val res203: Int = 0

scala> langs.length
val res204: Int = 3

scala> nums.length
val res205: Int = 4

scala> nums.mkString
val res206: String = 1234

scala> langs.mkString
val res207: String = PythonJavaScala

scala> langs.mkString("-")
val res209: String = Python-Java-Scala

scala> langs.mkString("->")
val res210: String = Python->Java->Scala

scala> nums.sum
val res211: Int = 10

scala> nums.take(2)
val res212: List[Int] = List(1, 2)

scala> langs.take(2)
val res213: List[String] = List(Python, Java)

scala> langs.takeRight(2)
val res215: List[String] = List(Java, Scala)

scala> langs.toArray
val res216: Array[String] = Array(Python, Java, Scala)

scala> langs.toString
val res217: String = List(Python, Java, Scala)

scala> nums.toString
val res218: String = List(1, 2, 3, 4)
```

## 1.3 Set
All elements in Set are unique.
```scala
scala> val set = Set(1,2,3)
val set: scala.collection.immutable.Set[Int] = Set(1, 2, 3)

scala> set.drop(1)
val res223: scala.collection.immutable.Set[Int] = Set(2, 3)

scala> set
val res224: scala.collection.immutable.Set[Int] = Set(1, 2, 3)

scala> import scala.collection.mutable.Set
import scala.collection.mutable.Set

scala> val set1 = Set(1,2,3)
val set1: scala.collection.mutable.Set[Int] = HashSet(1, 2, 3)

scala> set1.add(2)
val res226: Boolean = false

scala> set1
val res227: scala.collection.mutable.Set[Int] = HashSet(1, 2, 3)

scala> set1.add(4)
val res228: Boolean = true

scala> set1
val res229: scala.collection.mutable.Set[Int] = HashSet(1, 2, 3, 4)

scala> set1.remove(1)
val res230: Boolean = true

scala> set1
val res231: scala.collection.mutable.Set[Int] = HashSet(2, 3, 4)

scala> set1.head
val res232: Int = 2

scala> set1.tail
val res233: scala.collection.mutable.Set[Int] = HashSet(3, 4)

scala> set1.isEmpty
val res234: Boolean = false

scala> set
val res235: scala.collection.immutable.Set[Int] = Set(1, 2, 3)

scala> set1
val res236: scala.collection.mutable.Set[Int] = HashSet(2, 3, 4)

scala> set1.concat(set)
val res237: scala.collection.mutable.Set[Int] = HashSet(1, 2, 3, 4)

scala> set1
val res238: scala.collection.mutable.Set[Int] = HashSet(2, 3, 4)

scala> set1 ++ set
val res239: scala.collection.mutable.Set[Int] = HashSet(1, 2, 3, 4)

scala> set1.++(set)
val res240: scala.collection.mutable.Set[Int] = HashSet(1, 2, 3, 4)

scala> set1.max
val res242: Int = 4

scala> set1.min
val res243: Int = 2

scala> set1.sum
val res244: Int = 9

scala> set.intersect(set1)
val res245: scala.collection.immutable.Set[Int] = Set(2, 3)

scala> set.&(set1)
val res246: scala.collection.immutable.Set[Int] = Set(2, 3)

scala> set1.count((x:Int) => x>0)
val res251: Int = 3

scala> set1.drop(0)
val res252: scala.collection.mutable.Set[Int] = HashSet(2, 3, 4)

scala> set1
val res253: scala.collection.mutable.Set[Int] = HashSet(2, 3, 4)

scala> set1.drop(1)
val res254: scala.collection.mutable.Set[Int] = HashSet(3, 4)

scala> set1.exists((x:Int) => x>2)
val res256: Boolean = true

scala> set1.find((x:Int) => x>3)
val res257: Option[Int] = Some(4)

scala> set
val res258: scala.collection.immutable.Set[Int] = Set(1, 2, 3)

scala> set.forall((x:Int) => x>1)
val res260: Boolean = false

scala> set.foreach((x:Int) =>println(x))
1
2
3

scala> set.last
val res262: Int = 3

scala> set.map((x:Int) => println("ele: "+x))
ele: 1
ele: 2
ele: 3
val res263: scala.collection.immutable.Set[Unit] = Set(())

scala> set.mkString
val res264: String = 123

scala> set.mkString("->")
val res265: String = 1->2->3

scala> set.size
val res266: Int = 3

scala> set.splitAt(1)
val res267: (scala.collection.immutable.Set[Int], scala.collection.immutable.Set[Int]) = (Set(1),Set(2, 3))

scala> set.take(1)
val res268: scala.collection.immutable.Set[Int] = Set(1)

scala> set.takeRight(2)
val res269: scala.collection.immutable.Set[Int] = Set(2, 3)

scala> set.toArray
val res271: Array[Int] = Array(1, 2, 3)

scala> set.toString
val res273: String = Set(1, 2, 3)

```

## 1.4 Map
```scala
scala> val colors = Map("red" -> "#ff0000", "yellow" -> "#ffff00", "blue" -> "#0000ff")
val colors: scala.collection.immutable.Map[String,String] = Map(red -> #ff0000, yellow -> #ffff00, blue -> #0000ff)

scala> colors.keys
val res0: Iterable[String] = Set(red, yellow, blue)

scala> colors.values
val res1: Iterable[String] = Iterable(#ff0000, #ffff00, #0000ff)

scala> colors("red")
val res2: String = #ff0000

scala> colors("blue")
val res3: String = #0000ff

scala> val colors2 = Map("red" -> "#FF0000",
     |                         "azure" -> "#F0FFFF",
     |                         "peru" -> "#CD853F")
val colors2: scala.collection.immutable.Map[String,String] = Map(red -> #FF0000, azure -> #F0FFFF, peru -> #CD853F)

scala>

scala> colors ++ colors2
val res4: scala.collection.immutable.Map[String,String] = HashMap(blue -> #0000ff, azure -> #F0FFFF, peru -> #CD853F, yellow -> #ffff00, red -> #FF0000)

scala> colors.++(colors2)
val res5: scala.collection.immutable.Map[String,String] = HashMap(blue -> #0000ff, azure -> #F0FFFF, peru -> #CD853F, yellow -> #ffff00, red -> #FF0000)

scala> colors
val res6: scala.collection.immutable.Map[String,String] = Map(red -> #ff0000, yellow -> #ffff00, blue -> #0000ff)

scala> colors.keys.foreach(i => println(i+" => "+colors(i)))
red => #ff0000
yellow => #ffff00
blue => #0000ff

scala> colors.contains("red")
val res10: Boolean = true

scala> colors.contains("black")
val res11: Boolean = false

scala> colors.get("yellow")
val res12: Option[String] = Some(#ffff00)

scala> colors.addString(new StringBuilder)
val res13: StringBuilder = red -> #ff0000yellow -> #ffff00blue -> #0000ff

scala> colors.addString(new StringBuilder).toString
val res14: String = red -> #ff0000yellow -> #ffff00blue -> #0000ff

scala> colors
val res16: scala.collection.immutable.Map[String,String] = Map(red -> #ff0000, yellow -> #ffff00, blue -> #0000ff)

scala> colors.drop(1)
val res19: scala.collection.immutable.Map[String,String] = Map(yellow -> #ffff00, blue -> #0000ff)

scala> colors
val res20: scala.collection.immutable.Map[String,String] = Map(red -> #ff0000, yellow -> #ffff00, blue -> #0000ff)

scala> colors.dropRight(1)
val res21: scala.collection.immutable.Map[String,String] = Map(red -> #ff0000, yellow -> #ffff00)

scala> colors
val res24: scala.collection.immutable.Map[String,String] = Map(red -> #ff0000, yellow -> #ffff00, blue -> #0000ff)

scala> colors.exists(x => x._1 == "red")
val res27: Boolean = true

scala> colors.exists(x => x._1 == "red" && x._2.length > 0)
val res28: Boolean = true

def exists(p: ((String, String)) => Boolean): Boolean

scala> colors.filter(x => x._1.startsWith("r"))
val res30: scala.collection.immutable.Map[String,String] = Map(red -> #ff0000)

scala> colors.find(x => x._1.startsWith("r"))
val res34: Option[(String, String)] = Some((red,#ff0000))

scala> colors.foreach(x => x._1.startsWith("r"))

scala> colors.foreach(x => println(x._1 + " -> "+x._2))
red -> #ff0000
yellow -> #ffff00
blue -> #0000ff

scala> colors.init
val res37: scala.collection.immutable.Map[String,String] = Map(red -> #ff0000, yellow -> #ffff00)

scala> colors.isEmpty
val res39: Boolean = false

scala> colors.keys
val res40: Iterable[String] = Set(red, yellow, blue)

def last: (String, String)

scala> colors.last
val res41: (String, String) = (blue,#0000ff)

scala> colors.max
val res42: (String, String) = (yellow,#ffff00)

scala> val nums = Map("a" -> 10, "b" -> 20, "c" -> 15)
val nums: scala.collection.immutable.Map[String,Int] = Map(a -> 10, b -> 20, c -> 15)

scala> nums.max
val res43: (String, Int) = (c,15)

scala> nums.min
val res44: (String, Int) = (a,10)

scala> colors.mkString
val res46: String = red -> #ff0000yellow -> #ffff00blue -> #0000ff

scala> colors.mkString("; ")
val res47: String = red -> #ff0000; yellow -> #ffff00; blue -> #0000ff

scala> colors.size
val res49: Int = 3

scala> nums
val res52: scala.collection.immutable.Map[String,Int] = Map(a -> 10, b -> 20, c -> 15)

def sum[B >: (String, Int)](implicit num: scala.math.Numeric[B]): B
def sum(implicit num: scala.math.Numeric[(String, Int)]): (String, Int) (deprecated)

scala> colors.tail
val res54: scala.collection.immutable.Map[String,String] = Map(yellow -> #ffff00, blue -> #0000ff)

scala> colors.take(1)
val res55: scala.collection.immutable.Map[String,String] = Map(red -> #ff0000)

scala> colors.takeRight(1)
val res56: scala.collection.immutable.Map[String,String] = Map(blue -> #0000ff)

scala> val ca=colors.toArray
val ca: Array[(String, String)] = Array((red,#ff0000), (yellow,#ffff00), (blue,#0000ff))

scala> ca(0)
val res62: (String, String) = (red,#ff0000)

scala> ca(0)._1
val res63: String = red

scala> ca(0)._2
val res64: String = #ff0000
```
