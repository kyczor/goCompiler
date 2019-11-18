package main

import (
    "bufio"
    "encoding/base64"
    "fmt"
    "io/ioutil"
    "os"
)

func main() {
    // Open file on disk.
    f, _ := os.Open("encoded.zip")

    // Read entire go file into byte slice.
    reader := bufio.NewReader(f)
    content, _ := ioutil.ReadAll(reader)

    // Encode as base64.
    encoded := base64.StdEncoding.EncodeToString(content)

    // Print encoded data to console.
    fmt.Println("ENCODED: " + encoded)

	output, _ := os.Create("decoded.zip")
	decoded, _ := base64.StdEncoding.DecodeString(encoded)
	output.Write(decoded)
}



