package se.techlisbon.mrwhite

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class WordLoader {
    // Default fallback words
    private val defaultWords = listOf(
        "Coffee" to "Tea",
        "Pancake" to "Waffle",
        "Sushi" to "Rice",
        "Butter" to "Margarine",
        "Soup" to "Stew",
        "Burger" to "Sandwich",
        "Biscuit" to "Cookie",
        "Jam" to "Jelly",
        "Yogurt" to "Ice Cream",
        "Wine" to "Champagne",
        "Tofu" to "Cheese",
        "Rice" to "Quinoa",
        "Tortilla" to "Bread",
        "Muffin" to "Cupcake",
        "Tomato" to "Apple",
        "Onion" to "Garlic",
        "Salt" to "Sugar",
        "Honey" to "Sugar",
        "Water" to "Vodka",
        "Milk" to "Soy Milk",
        "Date" to "Fig",
        "Kiwi" to "Avocado",
        "Curry" to "Chili",
        "Cabbage" to "Lettuce",
        "Fries" to "Chips",
        "Ketchup" to "Mustard",
        "Bee" to "Wasp",
        "Dinosaur" to "Lizard",
        "Unicorn" to "Horse",
        "Frog" to "Toad",
        "Wolf" to "Dog",
        "Lion" to "Tiger",
        "Penguin" to "Puffin",
        "Shark" to "Dolphin",
        "Eagle" to "Falcon",
        "Butterfly" to "Moth",
        "Spider" to "Scorpion",
        "Crocodile" to "Alligator",
        "Duck" to "Swan",
        "Horse" to "Donkey",
        "Whale" to "Orca",
        "Mushroom" to "Moss",
        "Tree" to "Bush",
        "Sun" to "Star",
        "Sea" to "Lake",
        "Rain" to "Hail",
        "Tornado" to "Hurricane",
        "Copper" to "Bronze",
        "Ruby" to "Ring",
        "Bat" to "Rat",
        "Seal" to "Walrus",
        "Crane" to "Stork",
        "Desert" to "Camel",
        "Savannah" to "Safari",
        "Mountain" to "Volcano",
        "Seagull" to "Tide",
        "Cave" to "Tunnel",
        "Island" to "Peninsula",
        "Glacier" to "Iceberg",
        "Stream" to "River",
        "Wood" to "Timber",
        "Pen" to "Pencil",
        "Soap" to "Shampoo",
        "Towel" to "Napkin",
        "Fork" to "Spoon",
        "Sofa" to "Chair",
        "Sofa" to "Stool",
        "Pillow" to "Bed",
        "Blanket" to "Bed",
        "Glass" to "Cup",
        "Umbrella" to "Raincoat",
        "Glasses" to "Contact Lenses",
        "Watch" to "Bracelet",
        "Ring" to "Earring",
        "Hoodie" to "Jacket",
        "Shoe" to "Boot",
        "Glove" to "Mitten",
        "Suitcase" to "Backpack",
        "Laptop" to "Tablet",
        "Headphones" to "Earbuds",
        "Fan" to "Air Conditioner",
        "Oven" to "Microwave",
        "Broom" to "Mop",
        "Candle" to "Flashlight",
        "Suit" to "Tie",
        "Deck" to "Patio",
        "Check" to "Receipt",
        "Drill" to "Saw",
        "Cast" to "Mold",
        "Bolt" to "Screw",
        "File" to "Folder",
        "Press" to "Iron",
        "Cabinet" to "Drawer",
        "Rope" to "String",
        "Helmet" to "Cap",
        "Chain" to "Cuff",
        "Frame" to "Border",
        "Shutter" to "Blind",
        "Key" to "Code",
        "Pad" to "Mat",
        "Screen" to "Monitor",
        "Stapler" to "Puncher",
        "Cinema" to "Theater",
        "Library" to "Bookstore",
        "Prison" to "Zoo",
        "Hotel" to "Hospital",
        "School" to "University",
        "Museum" to "Gallery",
        "Bakery" to "Cafe",
        "Gym" to "Bench",
        "Church" to "Priest",
        "Elevator" to "Escalator",
        "Pool" to "Beach",
        "Bar" to "Pub",
        "City" to "Village",
        "Street" to "Road",
        "Lighthouse" to "Beacon",
        "Office" to "Garage",
        "Wall" to "Fence",
        "Manure" to "Farm",
        "Garden" to "Farm",
        "Garden" to "Park",
        "Batman" to "Superman",
        "Dumbledore" to "Gandalf",
        "Star Wars" to "Star Trek",
        "Mario" to "Luigi",
        "Vampire" to "Werewolf",
        "Zombie" to "Ghost",
        "Santa Claus" to "Jesus",
        "Sherlock Holmes" to "James Bond",
        "Tintin" to "Asterix",
        "Pokemon" to "Digimon",
        "Facebook" to "Instagram",
        "Youtube" to "TikTok",
        "Android" to "iPhone",
        "Angel" to "Fairy",
        "Cyclops" to "Giant",
        "Pirate" to "Viking",
        "King" to "Throne",
        "Love" to "Lust",
        "Smart" to "Cunning",
        "Salary" to "Wealth",
        "Holiday" to "Vacation",
        "Medicine" to "Science",
        "History" to "Legend",
        "Music" to "Noise",
        "Painting" to "Photo",
        "Gift" to "Bribe",
        "Secret" to "Lie",
        "Bank" to "Shore",
        "Tear" to "Rip",
        "Tie" to "Knot",
        "Strike" to "Protest",
        "Spring" to "Morning",
        "Night" to "Winter",
        "Knight" to "Sword",
        "Punch" to "Slap",
        "Match" to "Lighter",
        "Newspaper" to "Book",
        "Lawyer" to "Judge",
        "Discount" to "Receipt",
        "Ticket" to "Bus",
        "Spike" to "Thorn",
        "Cap" to "Lid",
        "Light" to "Feather",
        "Heavy" to "Dense",
        "Sharp" to "Pointy",
        "Smooth" to "Slick",
        "Car" to "Van",
        "Hotel" to "Resort",
        "Beach" to "Sand",
        "Ship" to "Boat",
        "Boat" to "Raft",
        "Train" to "Metro",
        "Clock" to "Schedule",
    )

    suspend fun loadWords(url: String): Result<List<Pair<String, String>>> = withContext(Dispatchers.IO) {
        try {
            if (url.isBlank()) {
                return@withContext Result.success(defaultWords)
            }

            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            if (connection.responseCode == 200) {
                val content = connection.inputStream.bufferedReader().use { it.readText() }
                val pairs = content.lines()
                    .filter { it.isNotBlank() && !it.startsWith("#") }
                    .mapNotNull { line ->
                        val parts = line.split(",").map { it.trim() }
                        if (parts.size == 2) parts[0] to parts[1] else null
                    }

                if (pairs.isEmpty()) {
                    return@withContext Result.failure(Exception("No valid word pairs found"))
                }

                Result.success(pairs)
            } else {
                Result.failure(Exception("Failed to load: HTTP ${connection.responseCode}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}