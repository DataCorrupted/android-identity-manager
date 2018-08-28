#include <string>
#include <boost/property_tree/ptree.hpp>
#include <boost/property_tree/json_parser.hpp>
#include <boost/property_tree/info_parser.hpp>

namespace ndn{
namespace ndncert{

typedef boost::property_tree::ptree JsonSection;

JsonSection getConfig(){
    std::string config =
"{\n \
  \"ca-list\":\n \
  [\n \
    {\n \
        \"ca-prefix\": \"/ndn/CA\",\n \
        \"ca-info\": \"NDN Testbed CA\",\n \
        \"probe\": \"Use the university/organization name as input\",\n \
        \"target-list\": \"Use your email address (edu preferred) as input\",\n \
        \"certificate\": \"Bv0CJAcsCANuZG4IBXNpdGUxCANLRVkICBG8IvRjFf8XCARzZWxmCAn9AAABWcgU2aUUCRgBAhkEADbugBX9AU8wggFLMIIBAwYHKoZIzj0CATCB9wIBATAsBgcqhkjOPQEBAiEA/////wAAAAEAAAAAAAAAAAAAAAD///////////////8wWwQg/////wAAAAEAAAAAAAAAAAAAAAD///////////////wEIFrGNdiqOpPns+u9VXaYhrxlHQawzFOw9jvOPD4n0mBLAxUAxJ02CIbnBJNqZnjhE50mt4GffpAEQQRrF9Hy4SxCR/i85uVjpEDydwN9gS3rM6D0oTlF2JjClk/jQuL+Gn+bjufrSnwPnhYrzjNXazFezsu2QGg3v1H1AiEA/////wAAAAD//////////7zm+q2nF56E87nKwvxjJVECAQEDQgAES9Cb9iANUNYmwt5bjwNW1mZgjzIkDJb6FTCdiYWnkMMIVxh2YDllphoWDEAPS6kqJczzCuhnGYpZCp9tTaYKGxZMGwEDHB0HGwgDbmRuCAVzaXRlMQgDS0VZCAgRvCL0YxX/F/0A/Sb9AP4PMTk3MDAxMDFUMDAwMDAw/QD/DzIwMzcwMTE3VDIxMjg0NhdIMEYCIQDXkR1hF3GiP7yLXq+0JBJfi9QC+hhAu/1Bykx+MWz6RAIhANwelBTxxZr2C5bD15mjfhWudK4I1tOb4b/9xWCHyM7F\"\n \
    }\n \
  ]\n \
}";

    std::istringstream ss(config);
    JsonSection json;
    boost::property_tree::json_parser::read_json(ss, json);
    return json;
}

} // namespace ndncert
} // namespace ndn

