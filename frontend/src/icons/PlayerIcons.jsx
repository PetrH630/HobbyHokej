import { FaPhone, FaUser, FaShieldAlt } from "react-icons/fa";
import { GiHockey } from "react-icons/gi";
import { PiHockeyFill } from "react-icons/pi";
import { VscJersey } from "react-icons/vsc";
import { IoShirt, IoShirtOutline } from "react-icons/io5";
import { FaCircle } from "react-icons/fa";

export const PhoneIcon = (props) => <FaPhone {...props} />;
export const UserIcon = (props) => <FaUser {...props} />;
export const AdminIcon = (props) => <FaShieldAlt {...props} />;
export const PlayerIcon = (props) => <GiHockey {...props} />;
export const PlayerStatusIcon = (props) => <PiHockeyFill {...props} />;
export const TeamDarkIcon = (props) => <IoShirt {...props} />;
export const TeamLightIcon = (props) => <IoShirtOutline {...props} />;
export const CurrentPlayerIcon = (props) => <FaCircle {...props} />;
