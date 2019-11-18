package main

import (
    "encoding/json"
    "fmt"
    "io/ioutil"
    "os"
    //"strconv"
)

type Data struct {
    Title   string `json:"title"`
    Name   string `json:"name"`
    Numbers    Nrs    `json:"numbers"`
}

type Nrs struct {
    Age int `json:"age"`
    Foot  int `json:"foot"`
}

func main() {
    jsonFile, err := os.Open("Data.json")
    if err != nil {
        fmt.Println(err)
    }

    fmt.Println("Successfully Opened Data.json")
    defer jsonFile.Close()

    byteValue, _ := ioutil.ReadAll(jsonFile)

    var myData Data

    json.Unmarshal(byteValue, &myData)

		twiceTheAge := myData.Numbers.Age + 100
		singleDataNumbers := Nrs{Age: twiceTheAge, Foot: myData.Numbers.Foot}
		singleData := Data{Title: myData.Title, Name: myData.Name, Numbers: singleDataNumbers}

	byteArray, err3 := json.MarshalIndent(singleData, "", "\t")
	if err3 != nil {
		fmt.Println(err3)
	}

	f,err4 := os.Create("Data2.json")
	if err4 != nil {
		fmt.Println(err)
		f.Close()
		return
	}

	_, err5 := f.Write(byteArray)
	if err5 != nil {
		fmt.Println(err)
		f.Close()
		return
	}

	f.Close()

	//fmt.Println(string(byteArray))
}



























